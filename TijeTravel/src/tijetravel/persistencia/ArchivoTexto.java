package tijetravel.persistencia;

public abstract class ArchivoTexto {
    protected static final String SEPARADOR = ";";

    protected boolean lineaVacia(String linea) {
        return linea == null || linea.trim().isEmpty();
    }

    protected String[] separarCampos(String linea) {
        return linea.split(SEPARADOR, -1);
    }
}
