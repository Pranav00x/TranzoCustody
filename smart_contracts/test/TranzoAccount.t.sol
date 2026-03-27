// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "forge-std/Test.sol";
import {EntryPoint} from "account-abstraction/core/EntryPoint.sol";
import {TranzoAccountFactory} from "../src/TranzoAccountFactory.sol";
import {TranzoAccount} from "../src/TranzoAccount.sol";
import {PackedUserOperation} from "account-abstraction/interfaces/PackedUserOperation.sol";
import {MessageHashUtils} from "@openzeppelin/contracts/utils/cryptography/MessageHashUtils.sol";

contract TranzoAccountTest is Test {
    using MessageHashUtils for bytes32;

    EntryPoint public entryPoint;
    TranzoAccountFactory public factory;
    
    address public ownerAddress;
    uint256 public ownerPrivateKey;

    address public destAddr;

    function setUp() public {
        EntryPoint ep = new EntryPoint();
        vm.etch(0x0000000071727De22E5E9d8BAf0edAc6f37da032, address(ep).code);
        entryPoint = EntryPoint(payable(0x0000000071727De22E5E9d8BAf0edAc6f37da032));

        factory = new TranzoAccountFactory();

        (ownerAddress, ownerPrivateKey) = makeAddrAndKey("owner");
        destAddr = makeAddr("destination");
    }

    function test_CreateAccount() public {
        address account = factory.createAccount(ownerAddress, 1);
        assertTrue(account != address(0));
        
        TranzoAccount ta = TranzoAccount(payable(account));
        assertEq(ta.owner(), ownerAddress);
    }
    
    function test_DirectExecute() public {
        address account = factory.createAccount(ownerAddress, 1);
        TranzoAccount ta = TranzoAccount(payable(account));
        
        vm.deal(account, 1 ether);
        
        vm.prank(ownerAddress);
        ta.execute(destAddr, 0.5 ether, "");
        
        assertEq(destAddr.balance, 0.5 ether);
    }

    function test_RevertIfNotOwnerDirectExecute() public {
        address account = factory.createAccount(ownerAddress, 1);
        TranzoAccount ta = TranzoAccount(payable(account));
        
        vm.prank(makeAddr("random"));
        vm.expectRevert(TranzoAccount.NotEntryPointOrOwner.selector);
        ta.execute(destAddr, 0, "");
    }

    function test_UserOpFlow() public {
        // 1. Compute address
        address accountAddr = factory.getAddress(ownerAddress, 2);
        
        // 2. Fund address
        vm.deal(accountAddr, 1 ether);
        
        // 3. Prepare UserOp
        PackedUserOperation memory userOp = _buildUserOp(
            accountAddr, 
            abi.encodeCall(factory.createAccount, (ownerAddress, 2)),
            abi.encodeCall(TranzoAccount.execute, (destAddr, 0.1 ether, ""))
        );

        // 4. Sign UserOp
        bytes32 userOpHash = entryPoint.getUserOpHash(userOp);
        (uint8 v, bytes32 r, bytes32 s) = vm.sign(ownerPrivateKey, userOpHash.toEthSignedMessageHash());
        userOp.signature = abi.encodePacked(r, s, v);

        // 5. Submit via EntryPoint
        PackedUserOperation[] memory ops = new PackedUserOperation[](1);
        ops[0] = userOp;

        entryPoint.handleOps(ops, payable(makeAddr("beneficiary")));

        // 6. Verify execution
        assertEq(destAddr.balance, 0.1 ether);
    }

    function _buildUserOp(
        address sender, 
        bytes memory initCode, 
        bytes memory callData
    ) internal view returns (PackedUserOperation memory) {
        return PackedUserOperation({
            sender: sender,
            nonce: 0,
            initCode: abi.encodePacked(address(factory), initCode),
            callData: callData,
            accountGasLimits: bytes32((uint256(2000000) << 128) | uint256(2000000)),
            preVerificationGas: 500000,
            gasFees: bytes32((uint256(10) << 128) | uint256(10)),
            paymasterAndData: bytes(""),
            signature: bytes("")
        });
    }
}
