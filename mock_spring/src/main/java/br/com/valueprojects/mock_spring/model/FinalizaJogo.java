package br.com.valueprojects.mock_spring.model;

import java.util.Calendar;
import java.util.List;

import infra.JogoDao;
import infra.SmsService;


public class FinalizaJogo {

    private int total = 0;
    private final JogoDao dao;
    private final SmsService smsService; 

    public FinalizaJogo(JogoDao dao) {
        this.dao = dao;
        this.smsService = null; 
    }

    public FinalizaJogo(JogoDao dao, SmsService smsService) {
        this.dao = dao;
        this.smsService = smsService;
    }

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

    public void finalizarEEnviarSms(Jogo jogo) {
        jogo.finaliza(); 

        dao.salvar(jogo); 

        if (smsService != null) {
            String nomeVencedor = jogo.getVencedor().getNome(); 
            smsService.enviar(nomeVencedor);
        }
    }
    
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