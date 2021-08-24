package com.egorbatik.mancala.service;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.egorbatik.mancala.model.Board;
import com.egorbatik.mancala.model.PlayerType;
import com.egorbatik.mancala.repository.BoardRepository;

/**
 * In this class is where magic occurrs, the game basically keeps the state of
 * the board and applies changes to it.
 * 
 * @author egorbatik
 *
 */
@Service
public class BoardServiceImpl implements BoardService {

	@Value("${STONES_PER_HOUSE:6}")
	private Integer STONES_PER_HOUSE;

	@Value("${HOUSES:6}")
	private Integer HOUSES;

	@Autowired
	private BoardRepository boardRepository;

	// Some validations

	private void validateNextPlayer(final Board board, final PlayerType player) {
		if (board.getNextPlayer() != player) {
			throw new InvalidParameterException();
		}
	}

	private void validateStones(final Integer stones) {
		if (stones == 0) {
			throw new InvalidParameterException();
		}
	}

	private Integer[] resolveCurrentHalfBoard(final PlayerType player, final Board board) {
		switch (player) {
		case TOP:
			return board.getTopPlayerBoard();
		case BOTTOM:
			return board.getBottomPlayerBoard();
		default:
			throw new InvalidParameterException();
		}
	}

	private Integer[] resolveOtherHalfBoard(final PlayerType player, final Board board) {
		switch (player) {
		case TOP:
			return board.getBottomPlayerBoard();
		case BOTTOM:
			return board.getTopPlayerBoard();
		default:
			throw new InvalidParameterException();
		}
	}

	private PlayerType resolveDefaultNextPlayer(final PlayerType player) {
		switch (player) {
		case TOP:
			return PlayerType.BOTTOM;
		case BOTTOM:
			return PlayerType.TOP;
		default:
			throw new InvalidParameterException();
		}
	}

	private Boolean captureCondition(final Integer currentHouse, Integer[] halfBoardCurrentPlayer) {
		return ((currentHouse < 7) && (halfBoardCurrentPlayer[currentHouse - 1] == 1));
	}

	private Boolean mustKeepTurn(final Integer currentHouse) {
		return (currentHouse == 7);
	}

	/**
	 * As is
	 */
	@Override
	public Board retriveNewBoard() {
		Board board = new Board();
		board.setTopPlayerBoard(new Integer[] { STONES_PER_HOUSE, STONES_PER_HOUSE, STONES_PER_HOUSE, STONES_PER_HOUSE,
				STONES_PER_HOUSE, STONES_PER_HOUSE, 0 });
		board.setBottomPlayerBoard(new Integer[] { STONES_PER_HOUSE, STONES_PER_HOUSE, STONES_PER_HOUSE, STONES_PER_HOUSE,
				STONES_PER_HOUSE, STONES_PER_HOUSE, 0 });
		board.setNextPlayer(PlayerType.TOP);
		board = boardRepository.save(board);
		return board;
	}

	/**
	 * The core method of the game, this one validates, changes the stone
	 * distribution and check finish conditions. Can be polished, but this V1.
	 */
	@Override
	public Board applyGame(final Long boardId, final PlayerType player, Integer house)
			throws InvalidParameterException {
		// Bring the board
		final Board board = retrieveBoard(boardId);

		// Prepare for spliting
		final Integer[] halfBoardCurrentPlayer, halfBoardOtherPlayer;

		// Checks if the requesting player is the next player
		validateNextPlayer(board, player);

		// Assigns the halfs of the board according to the current player
		halfBoardCurrentPlayer = resolveCurrentHalfBoard(player, board);
		halfBoardOtherPlayer = resolveOtherHalfBoard(player, board);

		// By default change the next player
		board.setNextPlayer(resolveDefaultNextPlayer(player));

		// If finished... no more changes...
		if (board.getNextPlayer() == PlayerType.FINISHED) {
			return board;
		}

		Integer stones = halfBoardCurrentPlayer[house - 1]; // Array indexing

		// Cannot distribute 0 stones.
		validateStones(stones);

		halfBoardCurrentPlayer[house - 1] = 0;
		Integer currentHouse = house;

		// Stone distribution
		while (stones > 0) {
			// Jump enemy's house
			if (currentHouse == 13) {
				currentHouse = 1;
			}

			// Droping stones
			if (currentHouse <= 6) {
				halfBoardCurrentPlayer[currentHouse]++;
			}
			if (currentHouse > 6) {
				halfBoardOtherPlayer[currentHouse % 7]++;
			}

			// Moving to next house and substracting stone
			currentHouse++;
			stones--;
		}

		// Verify & capture
		if (captureCondition(currentHouse, halfBoardCurrentPlayer)) {
			halfBoardCurrentPlayer[6] += halfBoardOtherPlayer[5 - (currentHouse - 1)];
			halfBoardOtherPlayer[5 - (currentHouse - 1)] = 0;
		}

		// Keep turn
		if (mustKeepTurn(currentHouse)) {
			board.setNextPlayer(player);
		}

		// Update board
		switch (player) {
		case TOP:
			board.setTopPlayerBoard(halfBoardCurrentPlayer);
			board.setBottomPlayerBoard(halfBoardOtherPlayer);
			break;
		case BOTTOM:
			board.setBottomPlayerBoard(halfBoardCurrentPlayer);
			board.setTopPlayerBoard(halfBoardOtherPlayer);
			break;
		default:
			break;
		}

		// Check Finish and apply finish distribution if necessary
		switch (board.getNextPlayer()) {
		case TOP:
			if (Collections.max(Arrays.asList(board.getTopPlayerBoard()).subList(0, board.getTopPlayerBoard().length - 1)) == 0) {
				Integer allStones = Stream.of(board.getBottomPlayerBoard())
						.collect(Collectors.summingInt(Integer::intValue));
				board.setBottomPlayerBoard(new Integer[] { 0, 0, 0, 0, 0, 0, allStones });
				board.setNextPlayer(PlayerType.FINISHED);
			}
			break;
		case BOTTOM:
			if (Collections
					.max(Arrays.asList(board.getBottomPlayerBoard()).subList(0, board.getBottomPlayerBoard().length - 1)) == 0) {
				Integer allStones = Stream.of(board.getTopPlayerBoard()).collect(Collectors.summingInt(Integer::intValue));
				board.setTopPlayerBoard(new Integer[] { 0, 0, 0, 0, 0, 0, allStones });
				board.setNextPlayer(PlayerType.FINISHED);
			}
			break;
		default:
			break;

		}

		boardRepository.save(board);
		return board;
	}

	/**
	 * Bring specific board
	 */
	@Override
	public Board retrieveBoard(final Long id) {
		final Board board = boardRepository.findById(id).get();
		return board;
	}

	/**
	 * Reload a saved board or bring a new one
	 */
	@Override
	public Board getBoard(final Optional<Long> id) {
		return id.map(idBoard -> retrieveBoard(idBoard)).orElse(retriveNewBoard());
	}

	/**
	 * Resolve the next player to play
	 */
	@Override
	public PlayerType resolvePlayer(Optional<String> player) throws InvalidParameterException {
		if (!player.isPresent()) {
			return PlayerType.TOP;
		}
		return PlayerType.valueOf(player.get().toUpperCase());

	}
}
