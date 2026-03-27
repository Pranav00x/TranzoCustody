// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/proxy/ERC1967/ERC1967Proxy.sol";
import "@openzeppelin/contracts/utils/Create2.sol";
import "./TranzoAccount.sol";

/**
 * @title TranzoAccountFactory
 * @dev A factory for deploying TranzoAccount user proxy contracts.
 * Allows calculating the deterministic CREATE2 address beforehand.
 */
contract TranzoAccountFactory {
    // The main implementation logic deployed once
    TranzoAccount public immutable accountImplementation;

    constructor(IEntryPoint _entryPoint) {
        // Deploy implementation once on factory creation
        accountImplementation = new TranzoAccount(_entryPoint);
    }

    /**
     * @dev Creates an account, and return its address.
     * Returns the address even if the account is already deployed.
     * Note that during UserOperation execution, this method is called only if the account is not deployed.
     * This method returns an existing account address so that it's safe to be called.
     */
    function createAccount(address owner, uint256 salt) public returns (TranzoAccount ret) {
        address addr = getAddress(owner, salt);
        uint codeSize = addr.code.length;
        if (codeSize > 0) {
            return TranzoAccount(payable(addr));
        }

        // Craft initializer call for Proxy
        bytes memory initCall = abi.encodeCall(TranzoAccount.initialize, (owner));
        
        // Deploy Proxy and initialize
        ERC1967Proxy proxy = new ERC1967Proxy{salt : bytes32(salt)}(
            address(accountImplementation),
            initCall
        );

        ret = TranzoAccount(payable(address(proxy)));
    }

    /**
     * @dev Calculates the address of a smart account without deploying it.
     * Useful for allowing users to receive funds to a gasless deterministically calculated address
     * before their first actual transaction.
     */
    function getAddress(address owner, uint256 salt) public view returns (address) {
        bytes memory initCall = abi.encodeCall(TranzoAccount.initialize, (owner));
        
        bytes32 initCodeHash = keccak256(
            abi.encodePacked(
                type(ERC1967Proxy).creationCode,
                abi.encode(address(accountImplementation), initCall)
            )
        );

        return Create2.computeAddress(bytes32(salt), initCodeHash);
    }
}
