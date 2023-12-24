package Sistemajogodexadrez.ChessLayer.chessPiece;

import Sistemajogodexadrez.ChessLayer.ChessPiece;
import Sistemajogodexadrez.ChessLayer.Color;
import Sistemajogodexadrez.bordeGame.Borde;

public class Rook extends ChessPiece {

    public Rook(Borde borde, Color color) {
        super(borde, color);
    }

    @Override
    public String toString(){
        return "R";
    }
    
    @Override
    public boolean[][] PossibleMoves() {
        boolean[][] mat = new boolean[getBorde().getLinha()][getBorde().getColunas()];
        return mat;
    }
}
