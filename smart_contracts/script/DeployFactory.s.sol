// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "forge-std/Script.sol";
import {TranzoAccountFactory} from "../src/TranzoAccountFactory.sol";

contract DeployFactoryScript is Script {
    function run() external {
        uint256 deployerPrivateKey = vm.envUint("PRIVATE_KEY");

        vm.startBroadcast(deployerPrivateKey);

        TranzoAccountFactory factory = new TranzoAccountFactory();
        console.log("TranzoAccountFactory deployed at:", address(factory));

        vm.stopBroadcast();
    }
}
