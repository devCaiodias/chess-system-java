package Sistemajogodexadrez.ChessLayer;

import java.util.ArrayList;
import java.util.List;

import Sistemajogodexadrez.ChessLayer.chessPiece.King;
import Sistemajogodexadrez.ChessLayer.chessPiece.Rook;
import Sistemajogodexadrez.bordeGame.Borde;
import Sistemajogodexadrez.bordeGame.Piece;
import Sistemajogodexadrez.bordeGame.Position;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Borde borde;

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
        nextTurn();
        return (ChessPiece)capturedPiece;
    }

    private Piece makeMove(Position source, Position target){
        Piece p = borde.removePiece(source);
        Piece capturePiece = borde.removePiece(target);
        borde.placePieces(p, target);

        if (capturePiece != null) {
            piecesOnTheBoard.remove(capturePiece);
            capturedPieces.add(capturePiece);
        }

        return capturePiece;
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

    private void placeNewPice(char coluna, int linha, ChessPiece piece){
        borde.placePieces(piece, new ChessPosition(coluna, linha).toPosition());
        piecesOnTheBoard.add(piece);
    }

    public void initialSetup(){
        placeNewPice('c', 1, new Rook(borde, Color.WHITE));
        placeNewPice('c', 2, new Rook(borde, Color.WHITE));
        placeNewPice('d', 2, new Rook(borde, Color.WHITE));
        placeNewPice('e', 2, new Rook(borde, Color.WHITE));
        placeNewPice('e', 1, new Rook(borde, Color.WHITE));
        placeNewPice('d', 1, new King(borde, Color.WHITE));

        placeNewPice('c', 7, new Rook(borde, Color.BLACK));
        placeNewPice('c', 8, new Rook(borde, Color.BLACK));
        placeNewPice('d', 7, new Rook(borde, Color.BLACK));
        placeNewPice('e', 7, new Rook(borde, Color.BLACK));
        placeNewPice('e', 8, new Rook(borde, Color.BLACK));
        placeNewPice('d', 8, new King(borde, Color.BLACK));   
    }

}
