package Sistemajogodexadrez.ChessLayer;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Sistemajogodexadrez.ChessLayer.chessPiece.Bishop;
import Sistemajogodexadrez.ChessLayer.chessPiece.King;
import Sistemajogodexadrez.ChessLayer.chessPiece.Knight;
import Sistemajogodexadrez.ChessLayer.chessPiece.Pawn;
import Sistemajogodexadrez.ChessLayer.chessPiece.Queen;
import Sistemajogodexadrez.ChessLayer.chessPiece.Rook;
import Sistemajogodexadrez.bordeGame.Borde;
import Sistemajogodexadrez.bordeGame.Piece;
import Sistemajogodexadrez.bordeGame.Position;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Borde borde;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();



    public ChessMatch(){
        borde = new Borde(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();


    }

    public int getTurn(){
        return turn;
    }

    public Color getcurrentPlayer(){
        return currentPlayer;
    }

    public boolean getCheck(){
        return check;
    }

    public boolean getCheckMate(){
        return checkMate;
    }

    public ChessPiece getEnPassantVulnerable(){
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted(){
        return promoted;
    }

    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat = new ChessPiece[borde.getLinha()][borde.getColunas()];
        for (int i = 0; i < borde.getLinha(); i++) {
            for (int j = 0; j < borde.getColunas(); j++) {
                mat[i][j] = (ChessPiece) borde.pieces(i, j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition){
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return borde.pieces(position).PossibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition){
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("You  can't put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece)borde.pieces(target);

        //Special move - Promotion
        promoted = null;
        if (movedPiece instanceof Pawn) {
            if ((movedPiece.getColor() == Color.WHITE && target.getLinha() == 0) || (movedPiece.getColor() == Color.BLACK && target.getLinha() == 7)) {
                promoted = (ChessPiece)borde.pieces(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        check = (testCheck(opponent(currentPlayer))) ? true : false;

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }else{
            nextTurn();
        }

        //Special move - En Passant
        if (movedPiece instanceof Pawn && (target.getLinha() == source.getColuna() - 2 || target.getLinha() == source.getColuna() + 2)) {
            enPassantVulnerable = movedPiece;
        }
        else {
            enPassantVulnerable = null;
        }

        return (ChessPiece)capturedPiece;
    }
    
    public ChessPiece replacePromotedPiece(String type){
        if (promoted == null) {
            throw new IllegalStateException("thes is no piece to be promoted");
        
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") & !type.equals("Q")) {
            throw new InvalidParameterException("Invalid type for promotion");
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = borde.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        borde.placePieces(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;
    }

    private ChessPiece newPiece(String type, Color color){
        if (type.equals("B")) {
            return new Bishop(borde, color);
        }
        if (type.equals("N")) {
            return new Knight(borde, color);
        }
        if (type.equals("Q")) {
            return new Queen(borde, color);
        }
            return new Rook(borde, color);
    }

    private Piece makeMove(Position source, Position target){
        ChessPiece p = (ChessPiece)borde.removePiece(source);
        p.increaseMoveCount();
        Piece capturePiece = borde.removePiece(target);
        borde.placePieces(p, target);

        if (capturePiece != null) {
            piecesOnTheBoard.remove(capturePiece);
            capturedPieces.add(capturePiece);
        }

        //Special move - Castling kingside rook
        if (p instanceof King && target.getColuna() == source.getColuna() + 2) {
            Position sourceT = new Position(source.getLinha(), source.getColuna() + 3);
            Position targetT = new Position(source.getLinha(), source.getColuna() + 1);
            ChessPiece rook = (ChessPiece)borde.removePiece(sourceT);
            borde.placePieces(rook, targetT);
            rook.increaseMoveCount();
            
        }
        
        //Special move - Castling queenside rook
        if (p instanceof King && target.getColuna() == source.getColuna() - 2) {
            Position sourceT = new Position(source.getLinha(), source.getColuna() - 4);
            Position targetT = new Position(source.getLinha(), source.getColuna() - 1);
            ChessPiece rook = (ChessPiece)borde.removePiece(sourceT);
            borde.placePieces(rook, targetT);
            rook.increaseMoveCount();
        }

        //Special move En Passant
        if (p instanceof Pawn) {
            if (source.getColuna() != target.getColuna() && capturePiece == null) {
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(target.getLinha() + 1 , target.getColuna());
                }
                else{
                    pawnPosition = new Position(target.getLinha() - 1 , target.getColuna());

                }
                capturePiece = borde.removePiece(pawnPosition);
                capturedPieces.add(capturePiece);
                piecesOnTheBoard.remove(capturePiece);
            }
        }

        return capturePiece;
    }

    private void  undoMove(Position source, Position target, Piece capturedPiece){
        ChessPiece p = (ChessPiece)borde.removePiece(target);
        p.decreaseMoveCount();
        borde.placePieces(p, source);

        if (capturedPiece != null)  {
            borde.placePieces(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

          //Special move - Castling kingside rook
        if (p instanceof King && target.getColuna() == source.getColuna() + 2) {
            Position sourceT = new Position(source.getLinha(), source.getColuna() + 3);
            Position targetT = new Position(target.getLinha(), target.getColuna() + 1);
            ChessPiece rook = (ChessPiece)borde.removePiece(targetT);
            borde.placePieces(rook, sourceT);
            rook.decreaseMoveCount();
            
        }
        
        //Special move - Castling queenside rook
        if (p instanceof King && target.getColuna() == source.getColuna() - 2) {
            Position sourceT = new Position(source.getLinha(), source.getColuna() - 4);
            Position targetT = new Position(source.getLinha(), source.getColuna() - 1);
            ChessPiece rook = (ChessPiece)borde.removePiece(targetT);
            borde.placePieces(rook, sourceT);
            rook.decreaseMoveCount();
            
        }

        //Special move En Passant
        if (p instanceof Pawn) {
            if (source.getColuna() != target.getColuna() && capturedPiece == enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece)borde.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, target.getColuna());
                }
                else{
                    pawnPosition = new Position(4, target.getColuna());

                }
                borde.placePieces(pawn, pawnPosition);
            }
        }
    }

    private void validateSourcePosition(Position position){
        if (!borde.ThereIsAPiece(position)) {
            throw new ChessException("N existir peça na posição de origem");
        }
        if (currentPlayer != ((ChessPiece)borde.pieces(position)).getColor()) {
            throw new ChessException("the chosen piece is not yours");
        }
        if (!borde.pieces(position).IsThereAnyPossibleMove()) {
            throw new ChessException("There is no possible moves for the chosen piece");
        }
    }

    private void validateTargetPosition(Position source, Position target){
        if (!borde.pieces(source).PossibleMove(target)) {
            throw new ChessException("The chosen piece can't move to target position");
        }
    }

    private void nextTurn(){
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(Color color){
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color){
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece)p;
            }
        }
        throw new IllegalStateException("there is no " + color + " King on the borde");
    }

    private boolean testCheck(Color color){
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> oppnentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
        for (Piece p : oppnentPieces) {
            boolean[][] mat = p.PossibleMoves();
            if (mat[kingPosition.getLinha()][kingPosition.getColuna()]) {
                return true;
            }
        }
        return false;
    }


    private boolean testCheckMate(Color color){
        if (!testCheck(color)) {
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());

        for (Piece p : list) {
            boolean[][] mat = p.PossibleMoves();
            for (int i = 0; i < borde.getLinha(); i++) {
                for (int j = 0; j < borde.getColunas(); j++) {
                    if (mat[i][j]) {
                        Position soucer = ((ChessPiece)p).getChessPosition().toPosition();
                        Position terget = new Position(i, j);
                        Piece capturedPiece = makeMove(soucer, terget);
                        boolean testCheck = testCheck(color);
                        undoMove(soucer, terget, capturedPiece);
                        if (!testCheck) {
                            return false;
                        }
                    }
                }   
            }
         }
        return true;
    }

    private void placeNewPice(char coluna, int linha, ChessPiece piece){
        borde.placePieces(piece, new ChessPosition(coluna, linha).toPosition());
        piecesOnTheBoard.add(piece);
    }

    public void initialSetup(){
        placeNewPice('a', 1, new Rook(borde, Color.WHITE));
        placeNewPice('b', 1, new Knight(borde, Color.WHITE));
        placeNewPice('c', 1, new Bishop(borde, Color.WHITE));
        placeNewPice('d', 1, new Queen(borde, Color.WHITE));
        placeNewPice('e', 1, new King(borde, Color.WHITE, this));
        placeNewPice('f', 1, new Bishop(borde, Color.WHITE));
        placeNewPice('g', 1, new Knight(borde, Color.WHITE));
        placeNewPice('h', 1, new Rook(borde, Color.WHITE));
        placeNewPice('a', 2, new Pawn(borde, Color.WHITE, this));
        placeNewPice('b', 2, new Pawn(borde, Color.WHITE, this));
        placeNewPice('c', 2, new Pawn(borde, Color.WHITE, this));
        placeNewPice('d', 2, new Pawn(borde, Color.WHITE, this));
        placeNewPice('e', 2, new Pawn(borde, Color.WHITE, this));
        placeNewPice('f', 2, new Pawn(borde, Color.WHITE, this));
        placeNewPice('g', 2, new Pawn(borde, Color.WHITE, this));
        placeNewPice('h', 2, new Pawn(borde, Color.WHITE, this));
        
        placeNewPice('a', 8, new Rook(borde, Color.BLACK));
        placeNewPice('b', 8, new Knight(borde, Color.BLACK));
        placeNewPice('c', 8, new Bishop(borde, Color.BLACK));
        placeNewPice('d', 8, new Queen(borde, Color.BLACK));
        placeNewPice('e', 8, new King(borde, Color.BLACK, this));
        placeNewPice('f', 8, new Bishop(borde, Color.BLACK));
        placeNewPice('g', 8, new Knight(borde, Color.BLACK));
        placeNewPice('h', 8, new Rook(borde, Color.BLACK));
        placeNewPice('a', 7, new Pawn(borde, Color.BLACK, this));
        placeNewPice('b', 7, new Pawn(borde, Color.BLACK, this));
        placeNewPice('c', 7, new Pawn(borde, Color.BLACK, this));
        placeNewPice('d', 7, new Pawn(borde, Color.BLACK, this));
        placeNewPice('e', 7, new Pawn(borde, Color.BLACK, this));
        placeNewPice('f', 7, new Pawn(borde, Color.BLACK, this));
        placeNewPice('g', 7, new Pawn(borde, Color.BLACK, this));
        placeNewPice('h', 7, new Pawn(borde, Color.BLACK, this));
    }

}
