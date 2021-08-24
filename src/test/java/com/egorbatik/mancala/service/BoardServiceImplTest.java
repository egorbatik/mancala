package com.egorbatik.mancala.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidParameterException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.egorbatik.mancala.model.Board;
import com.egorbatik.mancala.model.PlayerType;
import com.egorbatik.mancala.repository.BoardRepository;
import com.egorbatik.mancala.service.BoardService;


@SpringBootTest
public class BoardServiceImplTest {

	 @Autowired
	 private BoardService boardService;
	 
	 @Autowired
	 private BoardRepository boardRepository; //To force to change states.
	 
	 @Test
	 @DisplayName("New Empty board, must start with 6 stones at each house, starting with TOP")
	 /**
	  * Expected
	  * TOP     0  6 6 6 6 6 6      
	  * BOTTOM     6 6 6 6 6 6 0
	  */
	 public void TestNewEmptyBoard() {
		 Board board=boardService.getBoard(Optional.empty());
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 6,6,6,6,6,6,0});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 6,6,6,6,6,6,0});
		 assertEquals(board.getNextPlayer(),PlayerType.TOP);
		 boardRepository.delete(board);
	 }
	
	 @Test
	 @DisplayName("Apply change from player not matching the turn")
	 /**
	  * Expected
	  *  	Error
	  */
	 public void TestApplyingGameFromWrongPlayer() {
		 Board board=boardService.getBoard(Optional.empty());	
		 assertThrows(InvalidParameterException.class,()-> boardService.applyGame(board.getId(), PlayerType.BOTTOM, 3));
		 boardRepository.delete(board);
	 }
	 
	 @Test
	 @DisplayName("Apply change to new Game - TOP Player, House 6")
	 /**
	  * Start      X
	  *         6  5 4 3 2 1 0(Array positions means House -1)       
	  * TOP     0  6 6 6 6 6 6      
	  * BOTTOM     6 6 6 6 6 6 0
	  *            0 1 2 3 4 5 6
	  * Expected
	  * Start      
	  * TOP     1  0 6 6 6 6 6      
	  * BOTTOM     7 7 7 7 7 6 0
	  * Next Player 1
	  * 
	  */
	 public void TestFirstGame() {
		 Board board=boardService.getBoard(Optional.empty());
		 board = boardService.applyGame(board.getId(),PlayerType.TOP, 6);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 6,6,6,6,6,0,1});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 7,7,7,7,7,6,0});
		 boardRepository.delete(board);
	 }
	 
	 @Test
	 @DisplayName("Forced State all onces")
	 /**
	  * Expected
	  * Start      
	  * TOP     1  1 1 1 1 1 1       
	  * BOTTOM     1 1 1 1 1 1 1
	  * Next Player 1
	  * 
	  */
	 public void TestForcedState() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setTopPlayerBoard(new Integer[] { 1,1,1,1,1,1,1});
		 board.setBottomPlayerBoard(new Integer[] { 1,1,1,1,1,1,1});
		 boardRepository.save(board);
		 board = boardService.getBoard(Optional.of(board.getId()));
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 1,1,1,1,1,1,1});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 1,1,1,1,1,1,1 });
		 boardRepository.delete(board);
	 }
	 
	 @Test
	 @DisplayName("Apply turn change TOP-BOTTOM")
	 /**
	  * Start      X     (Turn: TOP)
	  *         6  5 4 3 2 1 0(Array positions means House -1)       
	  * TOP     0  6 6 6 6 6 6      
	  * BOTTOM     6 6 6 6 6 6 0
	  *            0 1 2 3 4 5 6
	  * Expected
	  * Start            (Turn: BOTTOM)
	  * TOP     1  0 6 6 6 6 6      
	  * BOTTOM     7 7 7 7 7 6 0
	  * Next Player 1
	  * 
	  */
	 public void TestTurnChange() {
		 Board board=boardService.getBoard(Optional.empty());
		 board = boardService.applyGame(board.getId(),PlayerType.TOP, 6);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 6,6,6,6,6,0,1});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 7,7,7,7,7,6,0});
		 assertEquals(board.getNextPlayer(),PlayerType.BOTTOM);
		 boardRepository.delete(board);
	 }
	 
	 @Test
	 @DisplayName("Apply turn keep and house picking TOP")
	 /**
	  * Start          X  (Turn: TOP)
	  *         6  5 4 3 2 1 0(Array positions means House -1)       
	  * TOP     0  3 3 3 3 3 3      
	  * BOTTOM     3 3 3 3 3 3 0
	  *            0 1 2 3 4 5 6
	  * Expected
	  * Start            (Turn: TOP)
	  * TOP     1  4 4 0 3 3 3      
	  * BOTTOM     3 3 3 3 3 3 0
	  * Next Player 1
	  * 
	  */
	 public void TestTurnKeep() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setTopPlayerBoard(new Integer[] { 3,3,3,3,3,3,0});
		 board.setBottomPlayerBoard(new Integer[] { 3,3,3,3,3,3,0});
		 boardRepository.save(board);
		 board = boardService.applyGame(board.getId(),PlayerType.TOP, 4);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 3,3,3,0,4,4,1});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 3,3,3,3,3,3,0});
		 assertEquals(board.getNextPlayer(),PlayerType.TOP);
		 boardRepository.delete(board);
	 }

	 @Test
	 @DisplayName("Apply turn change BOTTOM-TOP")
	 /**
	  * Start           (Turn: BOTTOM)
	  *         6  5 4 3 2 1 0(Array positions means House -1)       
	  * TOP     0  6 6 6 6 6 6      
	  * BOTTOM     6 6 6 6 6 6 0
	  *            0 1 2 3 4 5 6
	  *                    X
	  *                    
	  * Expected
	  * Start            (Turn: TOP)
	  * TOP     0  6 6 7 7 7 7      
	  * BOTTOM     6 6 6 6 0 7 1
	  * Next Player 1
	  * 
	  */
	 public void TestTurnChangeBottom() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setNextPlayer(PlayerType.BOTTOM);
		 boardRepository.save(board);
		 board = boardService.applyGame(board.getId(),PlayerType.BOTTOM, 5);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 7,7,7,7,6,6,0});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 6,6,6,6,0,7,1});
		 assertEquals(board.getNextPlayer(),PlayerType.TOP);
		 boardRepository.delete(board);
	 }
	 
	 @Test
	 @DisplayName("Apply turn keep and house picking BOTTOM")
	 /**
	  * Start             (Turn: BOTTOM)
	  *         6  5 4 3 2 1 0   (Array positions means House -1)       
	  * TOP     0  3 3 3 3 3 3      
	  * BOTTOM     3 3 3 3 3 3 0
	  *            0 1 2 3 4 5 6
	  *                  X  
	  * Expected
	  * Start            (Turn: BOTTOM)
	  * TOP     1  3 3 3 3 3 3      
	  * BOTTOM     3 3 3 0 4 4 1
	  * Next Player 1
	  * 
	  */
	 public void TestTurnKeepBottom() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setTopPlayerBoard(new Integer[] { 3,3,3,3,3,3,0});
		 board.setBottomPlayerBoard(new Integer[] { 3,3,3,3,3,3,0});
		 board.setNextPlayer(PlayerType.BOTTOM);
		 boardRepository.save(board);
		 board = boardService.applyGame(board.getId(),PlayerType.BOTTOM, 4);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 3,3,3,3,3,3,0});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 3,3,3,0,4,4,1});
		 assertEquals(board.getNextPlayer(),PlayerType.BOTTOM);
		 boardRepository.delete(board);
	 }
	 
	 @Test
	 @DisplayName("Finish from TOP")
	 /**
	  * Start            X    (Turn: TOP)
	  *         6  5 4 3 2 1 0   (Array positions means House -1)       
	  * TOP     0  3 3 3 3 3 3      
	  * BOTTOM     0 0 0 0 0 0 20
	  *            0 1 2 3 4 5 6
	  *                    
	  * After Game (Without clean up)
	  * Start           
	  * TOP     0  4 4 4 0 3 3      
	  * BOTTOM     0 0 0 0 0 0 20
	  * Finish
	  * 
	  * Expected
	  * Start            (Turn: BOTTOM)
	  * TOP     18 0 0 0 0 0 0      
	  * BOTTOM     0 0 0 0 0 0 20
	  * Finish 
	  * 
	  */
	 public void FinishFromTOP() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setTopPlayerBoard(new Integer[] { 3,3,3,3,3,3,0});
		 board.setBottomPlayerBoard(new Integer[] { 0,0,0,0,0,0,20});
		 boardRepository.save(board);
		 board = boardService.applyGame(board.getId(),PlayerType.TOP, 3);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 0,0,0,0,0,0,18});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 0,0,0,0,0,0,20});
		 assertEquals(board.getNextPlayer(),PlayerType.FINISHED);
		 boardRepository.delete(board);
	 }

	 
	 @Test
	 @DisplayName("Finish from BOTTOM")
	 /**
	  * Start                (Turn: Bottom)
	  *         6  5 4 3 2 1 0   (Array positions means House -1)       
	  * TOP     20 0 0 0 0 0 0      
	  * BOTTOM     3 3 3 3 3 3 0
	  *            0 1 2 3 4 5 6
	  *                X    
	  * After Game (Without clean up)
	  * Start            
	  * TOP     20 0 0 0 0 0 0      
	  * BOTTOM     3 3 0 4 4 4 0
	  * 
	  * Expected   
	  * After Game (Without clean up)
	  * Start            (Turn: BOTTOM)
	  * TOP     20 0 0 0 0 0 0      
	  * BOTTOM     0 0 0 0 0 0 18
	  * 
	  * 
	  */
	 public void TestFinishFromBottom() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setTopPlayerBoard(new Integer[] { 0,0,0,0,0,0,20});
		 board.setBottomPlayerBoard(new Integer[] { 3,3,3,3,3,3,0});
		 board.setNextPlayer(PlayerType.BOTTOM);
		 boardRepository.save(board);
		 
		 board = boardService.applyGame(board.getId(),PlayerType.BOTTOM, 3);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 0,0,0,0,0,0,20});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 0,0,0,0,0,0,18});
		 assertEquals(board.getNextPlayer(),PlayerType.FINISHED);
		 boardRepository.delete(board);
	 }

	 @Test
	 @DisplayName("Apply capture from TOP")
	 /**
	  * Start           (Turn: TOP)
	  *                  X
	  *         6  5 4 3 2 1 0(Array positions means House -1)       
	  * TOP     0  0 0 0 3 0 0      
	  * BOTTOM     6 6 6 6 6 6 0
	  *            0 1 2 3 4 5 6
      *
	  * 
	  * Expected
	  * Start            (Turn: BOTTOM)
	  * TOP     6  1 1 1 0 0 0      
	  * BOTTOM     0 6 6 6 6 6 0
	  * 
	  */
	 public void TestCaptureFromTop() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setTopPlayerBoard(new Integer[] { 0,0,3,0,0,0,0});
		 board.setBottomPlayerBoard(new Integer[] { 6,6,6,6,6,6,0});
		 boardRepository.save(board);
		 board = boardService.applyGame(board.getId(),PlayerType.TOP, 3);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 0,0,0,1,1,1,6});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 0,6,6,6,6,6,0});
		 assertEquals(board.getNextPlayer(),PlayerType.BOTTOM);
		 boardRepository.delete(board);
	 }

	 @Test
	 @DisplayName("Apply capture from BOTTOM")
	 /**
	  * Start           (Turn: BOTTOM)
	  *                  
	  *         6  5 4 3 2 1 0(Array positions means House -1)       
	  * TOP     0  6 6 6 6 6 6      
	  * BOTTOM     0 0 3 0 0 0 0
	  *            0 1 2 3 4 5 6
      *                X
	  * 
	  * Expected
	  * Start            (Turn: TOP)
	  * TOP     0  6 6 6 6 6 0      
	  * BOTTOM     0 0 0 1 1 1 6
	  * 
	  */
	 public void TestCaptureFromBottom() {
		 Board board=boardService.getBoard(Optional.empty());
		 board.setTopPlayerBoard(new Integer[] { 6,6,6,6,6,6,0} );
		 board.setBottomPlayerBoard(new Integer[] { 0,0,3,0,0,0,0});
		 board.setNextPlayer(PlayerType.BOTTOM);
		 boardRepository.save(board);
		 board = boardService.applyGame(board.getId(),PlayerType.BOTTOM, 3);
		 assertArrayEquals(board.getTopPlayerBoard(),new Integer[] { 0,6,6,6,6,6,0});
		 assertArrayEquals(board.getBottomPlayerBoard(),new Integer[] { 0,0,0,1,1,1,6});
		 assertEquals(board.getNextPlayer(),PlayerType.TOP);
		 boardRepository.delete(board);
	 }
	 
}
