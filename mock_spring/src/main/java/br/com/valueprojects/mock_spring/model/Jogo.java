package br.com.valueprojects.mock_spring.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class Jogo {

    private String descricao;
    private Calendar data;
    private List<Resultado> resultados;
    private boolean finaliza;
    private int id;
    
    // NOVO: Campo para armazenar o vencedor
    private Participante vencedor; 

    public Jogo(String descricao) {
        this(descricao, Calendar.getInstance());
    }
    
    public Jogo(String descricao, Calendar data) {
        this.descricao = descricao;
        this.data = data;
        this.resultados = new ArrayList<Resultado>();
    }
    
    // NOVO: Adicione este construtor se o seu Builder precisar definir o vencedor diretamente
    public Jogo(String descricao, Calendar data, Participante vencedor) {
        this(descricao, data);
        this.vencedor = vencedor;
    }
    
    public void anota(Resultado resultado) {
        if(resultados.isEmpty() || podeTerResultado(resultado.getParticipante())) {
            resultados.add(resultado);
        }
    }

    // ... (demais métodos como podeTerResultado, qtdDeResultadosDo, etc. mantidos)
    private boolean podeTerResultado(Participante participante) {
        return !ultimoResultadoRecebido().getParticipante().equals(participante) && qtdDeResultadosDo(participante) <5;
    }

    private int qtdDeResultadosDo(Participante participante) {
        int total = 0;
        for(Resultado l : resultados) {
            if(l.getParticipante().equals(participante)) total++;
        }
        return total;
    }

    private Resultado ultimoResultadoRecebido() {
        return resultados.get(resultados.size()-1);
    }

    public String getDescricao() {
        return descricao;
    }

    public List<Resultado> getResultados() {
        return Collections.unmodifiableList(resultados);
    }

    public Calendar getData() {
        return (Calendar) data.clone();
    }

    // Método que calcula e define o vencedor (lógica simplificada para compilação)
    public void finaliza() {
        this.finaliza = true;
        
        // Lógica Simplificada: Define um vencedor (ex: o primeiro participante do resultado)
        if (!resultados.isEmpty()) {
            this.vencedor = resultados.get(0).getParticipante();
        }
    }
    
    public boolean isFinalizado() {
        return finaliza;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public void setData(Calendar data) {
        this.data = data;
    }
    
    // NOVO MÉTODO OBRIGATÓRIO: Chamado por FinalizaJogo.java
    public Participante getVencedor() {
        // Se a lógica de finaliza() define o vencedor, ele será retornado aqui.
        return vencedor; 
    }
    
    // NOVO MÉTODO OPCIONAL: Setter para facilitar o uso no CriadorDeJogo ou testes
    public void setVencedor(Participante vencedor) {
        this.vencedor = vencedor;
    }
}