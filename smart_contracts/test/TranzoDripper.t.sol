// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "forge-std/Test.sol";
import {TranzoDripper} from "../src/TranzoDripper.sol";
import {ERC20} from "@openzeppelin/contracts/token/ERC20/ERC20.sol";

contract MockToken is ERC20 {
    constructor() ERC20("Mock", "MCK") {}
    function mint(address to, uint256 amount) external {
        _mint(to, amount);
    }
}

contract TranzoDripperTest is Test {
    TranzoDripper public dripper;
    MockToken public token;

    address public sender;
    address public recipient;

    function setUp() public {
        dripper = new TranzoDripper();
        token = new MockToken();

        sender = makeAddr("sender");
        recipient = makeAddr("recipient");

        // Enough for streams that lock up to 3000 ether (see withdraw/cancel tests)
        token.mint(sender, 10_000 ether);
    }

    function test_CreateStream() public {
        vm.startPrank(sender);
        token.approve(address(dripper), 1000 ether);
        
        uint256 start = block.timestamp;
        uint256 end = start + 30 days;
        
        uint256 streamId = dripper.createStream(
            recipient,
            address(token),
            1000 ether,
            start,
            end
        );
        vm.stopPrank();

        assertEq(streamId, 1);
        assertEq(token.balanceOf(address(dripper)), 1000 ether);
    }

    function test_WithdrawFromStream() public {
        vm.startPrank(sender);
        token.approve(address(dripper), 3000 ether);
        
        uint256 start = block.timestamp;
        uint256 end = start + 30 days; // 2592000 secs
        
        uint256 streamId = dripper.createStream(
            recipient,
            address(token),
            3000 ether,
            start,
            end
        );
        vm.stopPrank();

        // Warp 10 days
        vm.warp(start + 10 days);

        uint256 withdrawable = dripper.getWithdrawableAmount(streamId);
        assertEq(withdrawable, 1000 ether); // 10 days out of 30 days = 1/3

        dripper.withdrawFromStream(streamId);
        assertEq(token.balanceOf(recipient), 1000 ether);
    }

    function test_CancelStream() public {
        vm.startPrank(sender);
        token.approve(address(dripper), 3000 ether);
        
        uint256 start = block.timestamp;
        uint256 end = start + 30 days;
        
        uint256 streamId = dripper.createStream(
            recipient,
            address(token),
            3000 ether,
            start,
            end
        );
        
        // Warp 15 days
        vm.warp(start + 15 days);
        
        dripper.cancelStream(streamId);
        vm.stopPrank();

        // Sender should get 1500 back, recipient gets 1500
        assertEq(token.balanceOf(sender), 1500 ether);
        assertEq(token.balanceOf(recipient), 1500 ether);
        
        // Stream cancelled
        vm.expectRevert(TranzoDripper.StreamEndedOrCancelled.selector);
        dripper.withdrawFromStream(streamId);
    }
}
