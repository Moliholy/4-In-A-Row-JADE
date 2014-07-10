package multiagentes.jade.cuatroenraya;

/**
 *
 * @author Molina This class represents the model. Every object of this class
 * owns to one Player, so that every player has its own copy of the game status.
 */
public class Board {

    public static final int ROWS = 6; //FILES
    public static final int COLUMNS = 7; //COLUMNS
    private SquareStatus[][] _matrix;

    public Board() {
        this._matrix = new SquareStatus[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLUMNS; j++)
                _matrix[i][j] = SquareStatus.EMPTY;
    }

    /**
     * Imprime por consola el tablero. Se usa para tareas de debug.
     */
    public void printBoard() {
        System.out.println();
        for (int i = 0; i < COLUMNS; i++) {
            System.out.println();
            for (int j = 0; j < ROWS; j++)
                System.out.print(_matrix[i][j].toString() + " ");
        }
        System.out.println();
    }

    /**
     * Modifica el estado de una de las casillas del tablero.
     *
     * @param movement Posición de la casilla.
     * @param status Nuevo estado de la casilla.
     * @return the square filled, or -1 if not
     */
    public int doMovement(int movement, SquareStatus status) throws Exception {
        int row = -1;
        int column = movement;
        boolean found = false;
        for (int i = ROWS - 1; i >= 0 && !found; i--)
            if (_matrix[i][column] == SquareStatus.EMPTY) {
                row = i;
                found = true;
            }

        if (found) {
            _matrix[row][column] = status;
            return row * COLUMNS + column;
        }
        return -1;
    }

    /**
     * Acceso a la matriz que compone el tablero.
     *
     * @return Tablero actual.
     */
    public SquareStatus[][] getMatrix() {
        return _matrix;
    }
}
