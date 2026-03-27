// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "forge-std/Test.sol";
import {TranzoCardSession} from "../src/TranzoCardSession.sol";

contract MockCardSession is TranzoCardSession {
    address public owner;

    constructor() {
        owner = msg.sender;
    }

    function _requireOwnerOrEntryPoint() internal view override {
        require(msg.sender == owner, "Not owner");
    }
}

contract TranzoCardSessionTest is Test {
    MockCardSession public sessionModule;
    
    address public owner;
    address public sessionKey;

    function setUp() public {
        owner = address(this);
        sessionModule = new MockCardSession();
        sessionKey = makeAddr("sessionKey");
    }

    function test_AddSession() public {
        sessionModule.addSession(
            sessionKey,
            500e6, // $500 daily
            100e6, // $100 per tx
            makeAddr("usdc"),
            uint48(block.timestamp),
            uint48(block.timestamp + 30 days)
        );

        (
            address key,
            uint256 daily,
            uint256 perTx,
            uint256 spent,
            ,
            ,
            ,
            ,
            bool active
        ) = sessionModule.sessions(sessionKey);
        
        assertEq(key, sessionKey);
        assertEq(daily, 500e6);
        assertEq(perTx, 100e6);
        assertEq(spent, 0);
        assertTrue(active);
    }
    
    function test_ValidateSessionSpend() public {
        sessionModule.addSession(
            sessionKey,
            500e6,
            100e6,
            makeAddr("usdc"),
            uint48(block.timestamp),
            uint48(block.timestamp + 30 days)
        );
        
        sessionModule.validateSessionKeySpend(sessionKey, 50e6);
        
        (,,, uint256 spent,,,,,) = sessionModule.sessions(sessionKey);
        assertEq(spent, 50e6);
    }

    function test_RevertExceedsPerTxLimit() public {
        sessionModule.addSession(
            sessionKey,
            500e6,
            100e6,
            makeAddr("usdc"),
            uint48(block.timestamp),
            uint48(block.timestamp + 30 days)
        );
        
        vm.expectRevert(TranzoCardSession.ExceedsPerTxLimit.selector);
        sessionModule.validateSessionKeySpend(sessionKey, 150e6);
    }

    function test_RevertExceedsDailyLimit() public {
        sessionModule.addSession(
            sessionKey,
            500e6,
            100e6,
            makeAddr("usdc"),
            uint48(block.timestamp),
            uint48(block.timestamp + 30 days)
        );
        
        sessionModule.validateSessionKeySpend(sessionKey, 100e6);
        sessionModule.validateSessionKeySpend(sessionKey, 100e6);
        sessionModule.validateSessionKeySpend(sessionKey, 100e6);
        sessionModule.validateSessionKeySpend(sessionKey, 100e6);
        sessionModule.validateSessionKeySpend(sessionKey, 100e6); // total 500e6

        vm.expectRevert(TranzoCardSession.ExceedsDailyLimit.selector);
        sessionModule.validateSessionKeySpend(sessionKey, 50e6);
    }
}
