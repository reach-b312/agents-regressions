/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package regressions;

import java.util.Arrays;

/**
 *
 * @author Rich
 */
public class MultipleLinearRegression{
    double [][] X;
    double [][] Y;
    public MultipleLinearRegression(double [][] originalX,double [] y){
        X = originalX;
        for (double[] array : X) {
            System.out.println(Arrays.toString(array));
        }
        System.out.println(X[0].length);
        System.out.println(X.length);
        Y= new double [y.length][1];
        for (int i = 0; i<y.length;i++){
            Y[i][0] = y[i];
        }
    }
    public double [] fit(){
        return MatrixOperations.matrixToVector(MatrixOperations.multiply(MatrixOperations.invert(MatrixOperations.multiply(MatrixOperations.transpose(X),X)),MatrixOperations.multiply(MatrixOperations.transpose(X), Y)));
    }

}