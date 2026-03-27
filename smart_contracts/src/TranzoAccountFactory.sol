// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {Create2} from "@openzeppelin/contracts/utils/Create2.sol";
import {ERC1967Proxy} from "@openzeppelin/contracts/proxy/ERC1967/ERC1967Proxy.sol";
import {TranzoAccount} from "./TranzoAccount.sol";

/**
 * @title TranzoAccountFactory
 * @author Tranzo Team
 * @notice Factory for deploying TranzoAccount using CREATE2 deterministic addresses.
 */
contract TranzoAccountFactory {
    /// @notice The singleton TranzoAccount implementation contract.
    TranzoAccount public immutable accountImplementation;

    /// @notice Emitted when a new account is deployed.
    event AccountCreated(address indexed account, address indexed owner, uint256 salt);

    /**
     * @notice Deploy the implementation.
     */
    constructor() {
        accountImplementation = new TranzoAccount();
    }

    /**
     * @notice Deploy a new TranzoAccount proxy.
     * @param owner The EOA owner.
     * @param salt  A random value to ensure deterministic distinct addresses per user.
     * @return ret  The deployed account address.
     */
    function createAccount(address owner, uint256 salt) public returns (address ret) {
        address addr = getAddress(owner, salt);
        uint256 codeSize = addr.code.length;
        if (codeSize > 0) {
            return addr;
        }

        bytes memory initData = abi.encodeCall(TranzoAccount.initialize, (owner));
        bytes memory proxyCreationCode = abi.encodePacked(
            type(ERC1967Proxy).creationCode,
            abi.encode(address(accountImplementation), initData)
        );

        bytes32 combinedSalt = keccak256(abi.encodePacked(owner, salt));

        ret = Create2.deploy(0, combinedSalt, proxyCreationCode);
        emit AccountCreated(ret, owner, salt);
    }

    /**
     * @notice Compute the address of a smart account before deploying.
     * @param owner The EOA owner.
     * @param salt  The salt used during deployment.
     * @return The counterfactual deployment address.
     */
    function getAddress(address owner, uint256 salt) public view returns (address) {
        bytes memory initData = abi.encodeCall(TranzoAccount.initialize, (owner));
        bytes memory proxyCreationCode = abi.encodePacked(
            type(ERC1967Proxy).creationCode,
            abi.encode(address(accountImplementation), initData)
        );

        bytes32 combinedSalt = keccak256(abi.encodePacked(owner, salt));
        return Create2.computeAddress(combinedSalt, keccak256(proxyCreationCode));
    }
}
