package io.sim;

public class ReportsController {
    private static ManipuladorCSV arquivoBanco = new ManipuladorCSV("reports/banco.csv");
    private static ManipuladorCSV arquivoCars = new ManipuladorCSV("reports/cars.csv");
    private static ManipuladorCSV arquivoCompany = new ManipuladorCSV("reports/company.csv");

    public static ManipuladorCSV getArquivoBanco() {
        return arquivoBanco;
    }
    public static ManipuladorCSV getArquivoCars() {
        return arquivoCars;
    }
    public static ManipuladorCSV getArquivoCompany() {
        return arquivoCompany;
    }
}
