// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {IERC20} from "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import {SafeERC20} from "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";

/**
 * @title TranzoDripper
 * @author Tranzo Team
 * @notice Salary streaming contract supporting linear vesting.
 */
contract TranzoDripper {
    using SafeERC20 for IERC20;

    struct Stream {
        address sender;
        address recipient;
        address token;
        uint256 totalAmount;
        uint256 withdrawn;
        uint256 startTime;
        uint256 endTime;
        bool cancelled;
    }

    uint256 public nextStreamId = 1;
    mapping(uint256 => Stream) public streams;

    event StreamCreated(
        uint256 indexed streamId,
        address indexed sender,
        address indexed recipient,
        uint256 totalAmount,
        uint256 startTime,
        uint256 endTime
    );
    event Withdrawn(uint256 indexed streamId, address indexed recipient, uint256 amount);
    event StreamCancelled(uint256 indexed streamId, address indexed sender);

    error InvalidTimeRange();
    error ZeroAmount();
    error ZeroAddress();
    error Unauthorized();
    error StreamEndedOrCancelled();

    /**
     * @notice Create a linear vesting stream.
     */
    function createStream(
        address recipient,
        address token,
        uint256 totalAmount,
        uint256 startTime,
        uint256 endTime
    ) external returns (uint256) {
        if (recipient == address(0)) revert ZeroAddress();
        if (totalAmount == 0) revert ZeroAmount();
        if (startTime >= endTime) revert InvalidTimeRange();

        IERC20(token).safeTransferFrom(msg.sender, address(this), totalAmount);

        uint256 streamId = nextStreamId++;
        streams[streamId] = Stream({
            sender: msg.sender,
            recipient: recipient,
            token: token,
            totalAmount: totalAmount,
            withdrawn: 0,
            startTime: startTime,
            endTime: endTime,
            cancelled: false
        });

        emit StreamCreated(streamId, msg.sender, recipient, totalAmount, startTime, endTime);
        return streamId;
    }

    /**
     * @notice Get the amount currently accrued but unwithdrawn for a stream.
     */
    function getWithdrawableAmount(uint256 streamId) public view returns (uint256) {
        Stream memory stream = streams[streamId];
        if (stream.cancelled || block.timestamp <= stream.startTime) return 0;
        
        uint256 accrued;
        if (block.timestamp >= stream.endTime) {
            accrued = stream.totalAmount;
        } else {
            accrued = (stream.totalAmount * (block.timestamp - stream.startTime)) / (stream.endTime - stream.startTime);
        }

        if (accrued > stream.withdrawn) {
            return accrued - stream.withdrawn;
        }
        return 0;
    }

    /**
     * @notice Withdraw accrued tokens.
     */
    function withdrawFromStream(uint256 streamId) external {
        Stream storage stream = streams[streamId];
        if (stream.cancelled) revert StreamEndedOrCancelled();

        uint256 amount = getWithdrawableAmount(streamId);
        if (amount == 0) revert ZeroAmount();

        stream.withdrawn += amount;
        IERC20(stream.token).safeTransfer(stream.recipient, amount);

        emit Withdrawn(streamId, stream.recipient, amount);
    }

    /**
     * @notice Cancel a stream. Withdrawable amounts are sent to recipient, rest to sender.
     */
    function cancelStream(uint256 streamId) external {
        Stream storage stream = streams[streamId];
        if (msg.sender != stream.sender) revert Unauthorized();
        if (stream.cancelled) revert StreamEndedOrCancelled();

        uint256 withdrawable = getWithdrawableAmount(streamId);
        stream.cancelled = true;

        if (withdrawable > 0) {
            stream.withdrawn += withdrawable;
            IERC20(stream.token).safeTransfer(stream.recipient, withdrawable);
            emit Withdrawn(streamId, stream.recipient, withdrawable);
        }

        uint256 remaining = stream.totalAmount - stream.withdrawn;
        if (remaining > 0) {
            IERC20(stream.token).safeTransfer(stream.sender, remaining);
        }

        emit StreamCancelled(streamId, msg.sender);
    }
}
