// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "forge-std/Test.sol";
import {TranzoPaymaster} from "../src/TranzoPaymaster.sol";
import {EntryPoint} from "account-abstraction/core/EntryPoint.sol";
import {ERC20} from "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import {PackedUserOperation} from "account-abstraction/interfaces/PackedUserOperation.sol";
import {MessageHashUtils} from "@openzeppelin/contracts/utils/cryptography/MessageHashUtils.sol";
import {SIG_VALIDATION_FAILED} from "account-abstraction/core/Helpers.sol";

contract MockUSDC is ERC20 {
    constructor() ERC20("USDC", "USDC") {}
    function mint(address to, uint256 amount) external {
        _mint(to, amount);
    }
}

contract TranzoPaymasterTest is Test {
    using MessageHashUtils for bytes32;

    TranzoPaymaster public paymaster;
    EntryPoint public entryPoint;
    MockUSDC public usdc;
    
    address public signerAddr;
    uint256 public signerKey;
    
    address public ownerAddr;

    function setUp() public {
        (signerAddr, signerKey) = makeAddrAndKey("signer");
        ownerAddr = makeAddr("owner");

        EntryPoint ep = new EntryPoint();
        vm.etch(0x0000000071727De22E5E9d8BAf0edAc6f37da032, address(ep).code);
        entryPoint = EntryPoint(payable(0x0000000071727De22E5E9d8BAf0edAc6f37da032));

        usdc = new MockUSDC();

        paymaster = new TranzoPaymaster(
            entryPoint,
            signerAddr,
            ownerAddr,
            address(usdc)
        );
        
        vm.deal(address(paymaster), 1 ether);
        paymaster.deposit{value: 1 ether}();
    }

    function test_ValidatePaymasterUserOp() public {
        PackedUserOperation memory userOp = _dummyUserOp();
        
        uint48 validUntil = uint48(block.timestamp + 1 hours);
        uint48 validAfter = uint48(block.timestamp);
        uint8 mode = 1; // Sponsored

        bytes32 userOpHash = entryPoint.getUserOpHash(userOp);
        bytes32 hash = keccak256(abi.encode(userOpHash, validUntil, validAfter));
        (uint8 v, bytes32 r, bytes32 s) = vm.sign(signerKey, hash.toEthSignedMessageHash());

        userOp.paymasterAndData = abi.encodePacked(
            address(paymaster),
            validUntil,
            validAfter,
            mode,
            r, s, v
        );

        vm.prank(address(entryPoint));
        (bytes memory context, uint256 validationData) = paymaster.validatePaymasterUserOp(userOp, userOpHash, 100000);
        
        assertEq(uint160(validationData), 0); // Authorizer success = 0

        // Parse context
        (address sender, uint8 parsedMode) = abi.decode(context, (address, uint8));
        assertEq(sender, userOp.sender);
        assertEq(parsedMode, 1);
    }
    
    function test_RevertInvalidSigner() public {
        PackedUserOperation memory userOp = _dummyUserOp();
        
        uint48 validUntil = uint48(block.timestamp + 1 hours);
        uint48 validAfter = uint48(block.timestamp);
        uint8 mode = 1;

        bytes32 userOpHash = entryPoint.getUserOpHash(userOp);
        bytes32 hash = keccak256(abi.encode(userOpHash, validUntil, validAfter));
        
        // Sign with wrong key
        (, uint256 wrongKey) = makeAddrAndKey("wrong");
        (uint8 v, bytes32 r, bytes32 s) = vm.sign(wrongKey, hash.toEthSignedMessageHash());

        userOp.paymasterAndData = abi.encodePacked(
            address(paymaster),
            validUntil,
            validAfter,
            mode,
            r, s, v
        );

        vm.prank(address(entryPoint));
        (, uint256 validationData) = paymaster.validatePaymasterUserOp(userOp, userOpHash, 100000);
        
        assertEq(uint160(validationData), 1); // Authorizer failed = 1
    }

    function _dummyUserOp() internal returns (PackedUserOperation memory) {
        return PackedUserOperation({
            sender: makeAddr("sender"),
            nonce: 0,
            initCode: bytes(""),
            callData: bytes(""),
            accountGasLimits: bytes32((uint256(2000000) << 128) | uint256(2000000)),
            preVerificationGas: 500000,
            gasFees: bytes32((uint256(10) << 128) | uint256(10)),
            paymasterAndData: bytes(""),
            signature: bytes("")
        });
    }
}
