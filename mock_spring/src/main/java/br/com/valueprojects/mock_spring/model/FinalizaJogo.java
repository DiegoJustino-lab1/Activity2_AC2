package br.com.valueprojects.mock_spring.model;

import java.util.Calendar;
import java.util.List;

import infra.JogoDao;
import infra.SmsService; // NOVO: Import do serviço de SMS


public class FinalizaJogo {

    private int total = 0;
    private final JogoDao dao;
    private final SmsService smsService; // NOVO: Campo para o serviço de SMS

    // Construtor principal (mantido o anterior para compatibilidade nos testes antigos)
    public FinalizaJogo(JogoDao dao) {
        this.dao = dao;
        this.smsService = null; // Inicializa como null se o teste antigo for rodado
    }
    
    // NOVO: Construtor para a nova regra de negócio (usado nos novos testes)
    public FinalizaJogo(JogoDao dao, SmsService smsService) {
        this.dao = dao;
        this.smsService = smsService;
    }

    // Método original (para compatibilidade com os testes antigos)
    public void finaliza() {
        List<Jogo> todosJogosEmAndamento = dao.emAndamento();

        for (Jogo jogo : todosJogosEmAndamento) {
            if (iniciouSemanaAnterior(jogo)) {
                jogo.finaliza();
                total++;
                dao.atualiza(jogo);
            }
        }
    }

    // NOVO: Método para a nova Regra de Negócio
    public void finalizarEEnviarSms(Jogo jogo) {
        // Lógica de finalização (se aplicável, ou apenas para garantir o vencedor)
        jogo.finaliza(); 

        // 1. SALVAR NA BASE DE DADOS (OBRIGATÓRIO)
        // Se dao.salvar() lançar uma exceção, o código para aqui, e o SMS não é enviado.
        dao.salvar(jogo); 

        // 2. ENVIAR SMS AO VENCEDOR (CONDICIONAL AO SALVAMENTO)
        if (smsService != null) {
            // Supondo que 'Jogo' tem um método que retorna o nome do vencedor
            String nomeVencedor = jogo.getVencedor().getNome(); 
            smsService.enviar(nomeVencedor);
        }
    }
    
    // Métodos auxiliares (mantidos)
    private boolean iniciouSemanaAnterior(Jogo jogo) {
        return diasEntre(jogo.getData(), Calendar.getInstance()) >= 7;
    }

    private int diasEntre(Calendar inicio, Calendar fim) {
        Calendar data = (Calendar) inicio.clone();
        int diasNoIntervalo = 0;
        while (data.before(fim)) {
            data.add(Calendar.DAY_OF_MONTH, 1);
            diasNoIntervalo++;
        }

        return diasNoIntervalo;
    }

    public int getTotalFinalizados() {
        return total;
    }
}