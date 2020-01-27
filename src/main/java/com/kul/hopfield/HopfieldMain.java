package com.kul.hopfield;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.Hopfield;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class HopfieldMain {


    public static void main(String[] args) throws IOException {
        DataSet trainingSet = new DataSet(3);
        trainingSet.addRow(new DataSetRow(new double[]{0.0, -1.0, -3.0}));
        trainingSet.addRow(new DataSetRow(new double[]{-1.0, 0.0, 2.0}));
        trainingSet.addRow(new DataSetRow(new double[]{-3.0, 2.0, 0.0}));


        Hopfield hopfield = new Hopfield(3);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Podaj wektor: ");
        String input = br.readLine();
        String[] inputArray = input.split(" ");
        double[] vector = Arrays.stream(inputArray)
                .mapToDouble(Double::parseDouble)
                .toArray();
        hopfield.setInput(vector);

        System.out.print("Podaj tryb (S - synchronczny, A - asynchroniczny): ");
        String mode = br.readLine();
        if ("S".equals(mode)) {
            hopfield.learn(trainingSet);
        } else {
            hopfield.learnInNewThread(trainingSet);
        }

        System.out.println("Testowanie sieci: ");
        System.out.println("Badanie punktu w trybie synchronicznym: ");

        int step = 0;
        boolean nextStep = true;

        StringBuilder previousResultdata = new StringBuilder();
        double previousEnergyValue = 0;
        while (nextStep) {

            StringBuilder inputData = new StringBuilder();
            StringBuilder resultData = new StringBuilder();
            double energyValue = 0;
            for (DataSetRow dataRow : trainingSet.getRows()) {
                hopfield.setInput(dataRow.getInput());
                hopfield.calculate();

                inputData.append(calculateInputPotential(dataRow, vector[step])).append(" ");

                double[] networkOutput = hopfield.getOutput();
                resultData.append(calculateOutputPotential(networkOutput, vector[step])).append(" ");

                energyValue = calculateEnergyFunction(networkOutput, vector[step]);
            }

            System.out.println("Potencjał wejściowy U: ");
            System.out.println(inputData);

            System.out.println("Potencjał wyjściowy V: ");
            System.out.println(resultData);

            System.out.println("Wartość funkcji energii: ");
            System.out.println(energyValue);

            if (resultData.toString().equals(previousResultdata.toString())) {
                System.out.println("Sieć ustabilizowała się");
                break;
            } else if (energyValue == previousEnergyValue) {
                System.out.println("Oscylacja dwupunktowa, punkty oscylacji: ");
                System.out.println(inputData);
                System.out.println(resultData);
                break;
            } else {
                previousResultdata = resultData;
                previousEnergyValue = energyValue;
            }
        }

    }

    private static double calculateInputPotential(DataSetRow dataSetRow, double vectorValue) {
        double sum = Arrays.stream(dataSetRow.getInput()).sum();
        return sum * vectorValue;
    }

    private static double calculateOutputPotential(double[] networkOutput, double vectorValue) {
        double sum = Arrays.stream(networkOutput).sum();
        return sum * vectorValue;
    }

    private static double calculateEnergyFunction(double[] networkOutput, double vectorValue) {
        return Arrays.stream(networkOutput).map(output -> vectorValue * output).sum() * networkOutput.length * 2;
    }
}
