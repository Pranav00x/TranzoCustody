// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "forge-std/Script.sol";
import {TranzoAccountFactory} from "../src/TranzoAccountFactory.sol";
import {TranzoPaymaster} from "../src/TranzoPaymaster.sol";
import {TranzoDripper} from "../src/TranzoDripper.sol";
import {IEntryPoint} from "account-abstraction/interfaces/IEntryPoint.sol";

contract DeployScript is Script {
    IEntryPoint public constant ENTRY_POINT = IEntryPoint(0x0000000071727De22E5E9d8BAf0edAc6f37da032);
    
    function run() external {
        uint256 deployerPrivateKey = vm.envUint("PRIVATE_KEY");
        address verifyingSigner = vm.envAddress("PAYMASTER_SIGNER");
        address initialOwner = vm.addr(deployerPrivateKey);
        address usdcAddress = vm.envAddress("USDC_ADDRESS");

        vm.startBroadcast(deployerPrivateKey);

        // 1. TranzoAccountFactory
        TranzoAccountFactory factory = new TranzoAccountFactory();
        console.log("TranzoAccountFactory deployed at:", address(factory));

        // 2. TranzoPaymaster
        TranzoPaymaster paymaster = new TranzoPaymaster(
            ENTRY_POINT,
            verifyingSigner,
            initialOwner,
            usdcAddress
        );
        console.log("TranzoPaymaster deployed at:", address(paymaster));

        // 3. Deposit ETH for Paymaster
        paymaster.deposit{value: 0.1 ether}();
        console.log("Paymaster deposited 0.1 ETH to EntryPoint");

        // 4. TranzoDripper
        TranzoDripper dripper = new TranzoDripper();
        console.log("TranzoDripper deployed at:", address(dripper));

        vm.stopBroadcast();
    }
}
