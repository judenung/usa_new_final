package com.example.application.backends;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Matrix {
    public short row;
    public short col;
    public BigDecimal[][] data;

    public static final int precision = 15;

    public Matrix (short row, short col) {
        this.row = row;
        this.col = col;
        data = new BigDecimal[row][col];
        for (short i = 0; i < row; i++) {
            for (short j = 0; j < col; j++) {
                this.data[i][j] = new BigDecimal("0");
            }
        }
    }

    public Matrix (short row, short col, BigDecimal[][] data) {
        this.row = row;
        this.col = col;
        this.data = data;
    }

    public void insert (BigDecimal[][] data) {
        this.data = data;
    }

    public void insert (Matrix m) {
        this.data = m.data;
    }

    public void print() {
        for (short i = 0; i < this.row; i++) {
            for (short j = 0; j < this.col; j++) {
//                System.out.print(Math.round(data[i][j] * 1000000.0) / 1000000.0 + "  ");
                System.out.print(data[i][j]  + "  ");
            }
            System.out.println();
        }
    }

    public Matrix add (Matrix m) {
        if (this.row != m.row || this.col != m.col) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix(row, col);
        for (short i = 0; i < row; i++) {
            for (short j = 0; j < col; j++) {
                r.data[i][j] = this.data[i][j].add(m.data[i][j]).stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
            }
        }
        return r;
    }

    public Matrix subtract (Matrix m) {
        if (this.row != m.row || this.col != m.col) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix(row, col);
        for (short i = 0; i < row; i++) {
            for (short j = 0; j < col; j++) {
                r.data[i][j] = this.data[i][j].subtract(m.data[i][j]).stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
            }
        }
        return r;
    }

    public Matrix scalarMultiply (BigDecimal c) {
        Matrix r = new Matrix(row, col);
        for (short i = 0; i < row; i++) {
            for (short j = 0; j < col; j++) {
                r.data[i][j] = this.data[i][j].multiply(c).stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
            }
        }
        return r;
    }

    public Matrix multiply (Matrix m) {
        if (this.col != m.row) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix(this.row, m.col);
        for (short i = 0; i < r.row; i++) {
            for (short j = 0; j < r.col; j++) {
                for (short k = 0; k < this.col; k++) {
                    r.data[i][j] = r.data[i][j].add(this.data[i][k].multiply(m.data[k][j])).stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
                }
            }
        }
        return r;
    }

    public Matrix transpose() {
        Matrix r = new Matrix(col, row);
        for (short i = 0; i < col; i++) {
            for (short j = 0; j < row; j++) {
                r.data[i][j] = this.data[j][i].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
            }
        }
        return r;
    }

    public Matrix eliminateGaussJordan () {
        if (this.row != this.col - 1) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix(row, col);
        r.insert(this.data);
        for (short i = 0; i < row; i++) {
            BigDecimal c = r.data[i][i];
            if (c.compareTo(BigDecimal.ZERO) != 0) {
                for (short j = 0; j < col; j++) {
                    r.data[i][j] = r.data[i][j].divide(c, precision, RoundingMode.FLOOR).stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
                }
            }
            for (short k = 0; k < row; k++) {
                if (k != i) {
                    BigDecimal d = r.data[k][i];
                    for (short m = i; m < col; m++) {
                        r.data[k][m] = r.data[k][m].subtract(r.data[i][m].multiply(d)).stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
                    }
                }
            }
        }
        return r;
    }

    public Matrix appendRow (Matrix m) {
        if ( this.col != m.col) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix((short) (this.row + m.row), col);
        for (short i = 0; i < this.row + m.row; i++) {
            for (short j = 0; j < col; j++) {
                if (i < this.row) {
                    r.data[i][j] = this.data[i][j].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
                }
                else {
                    r.data[i][j] = m.data[i - this.row][j].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
                }

            }
        }
        return r;
    }

    public Matrix appendCol (Matrix m) {
        if ( this.row != m.row) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix(row, (short) (this.col + m.col));
        for (short i = 0; i < this.row; i++) {
            for (short j = 0; j < this.col + m.col; j++) {
                if (j < this.col) {
                    r.data[i][j] = this.data[i][j].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
                }
                else {
                    r.data[i][j] = m.data[i][j - this.col].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
                }

            }
        }
        return r;
    }

    public Matrix getRow (short row) {
        if (this.row <= row) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix((short) 1, col);
        for (short i = 0; i < col; i++) {
            r.data[0][i] = this.data[row][i].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
        }
        return r;
    }

    public Matrix getCol (short col) {
        if (this.col <= col) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix(row, (short) 1);
        for (short i = 0; i < row; i++) {
            r.data[i][0] = this.data[i][col].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
        }
        return r;
    }

    public Matrix getSubMatrix (short sRow, short eRow, short sCol, short eCol) {
        if (this.row <= eRow || sRow > eRow || this.col <= eCol || sCol > eCol) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix r = new Matrix((short) (eRow - sRow + 1), (short) (eCol - sCol + 1));
        for (short i = sRow; i <= eRow; i++) {
            for (short j = sCol; j <= eCol; j++) {
                r.data[i - sRow][j - sCol] = this.data[i][j].stripTrailingZeros().setScale(precision, RoundingMode.FLOOR);
            }
        }
        return r;
    }

}
