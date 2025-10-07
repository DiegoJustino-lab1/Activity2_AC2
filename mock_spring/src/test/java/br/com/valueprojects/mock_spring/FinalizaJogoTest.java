package br.com.valueprojects.mock_spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.FinalizaJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import infra.JogoDao;
import infra.SmsService;

@ExtendWith(MockitoExtension.class)
public class FinalizaJogoTest {

    @Mock
    private JogoDao daoMock;

    @Mock
    private SmsService smsMock;

    @InjectMocks
    private FinalizaJogo finalizador;

    @Test
    public void deveFinalizarJogosDaSemanaAnterior() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Jogo jogo1 = new CriadorDeJogo().para("Caça moedas").naData(antiga).constroi();
        Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();
        List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

        when(daoMock.emAndamento()).thenReturn(jogosAnteriores);

        finalizador.finaliza();

        assertTrue(jogo1.isFinalizado());
        assertTrue(jogo2.isFinalizado());
        assertEquals(2, finalizador.getTotalFinalizados());
    }

    @Test
    public void deveVerificarSeMetodoAtualizaFoiInvocado() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Jogo jogo1 = new CriadorDeJogo().para("Cata moedas").naData(antiga).constroi();
        List<Jogo> jogosAnteriores = Arrays.asList(jogo1);

        when(daoMock.emAndamento()).thenReturn(jogosAnteriores);

        finalizador.finaliza();

        verify(daoMock, times(1)).atualiza(jogo1);
    }

    @Test
    public void deveSalvarAntesDeEnviarSms() {
        Jogo jogo = new CriadorDeJogo().para("Corrida").constroi();
        jogo.setVencedor(new Participante("Vencedor"));

        finalizador.finalizarEEnviarSms(jogo);

        verify(daoMock).salvar(jogo);
        verify(smsMock).enviar("Vencedor");
    }

    @Test
    public void naoDeveEnviarSmsSeNaoSalvar() {
        verifyNoInteractions(smsMock);
    }

    @Test
    public void deveSalvarJogosFinalizadosEEnviarSmsApenasAposSalvar() {
        Calendar antiga = Calendar.getInstance();
        antiga.add(Calendar.DAY_OF_YEAR, -10);
        Participante vencedor = new Participante("João Vencedor");
        Jogo jogo1 = new CriadorDeJogo().para("Corrida Maluca").naData(antiga).resultado(vencedor, 500).constroi();

        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo1));

        finalizador.finalizaEEnviaSms();

        InOrder inOrder = inOrder(daoMock, smsMock);
        inOrder.verify(daoMock, times(1)).atualiza(jogo1);
        inOrder.verify(smsMock, times(1)).enviar("João Vencedor");
        assertTrue(jogo1.isFinalizado());
    }

    @Test
    public void naoDeveEnviarSmsSeOcorrerErroAoSalvar() {
        Calendar antiga = Calendar.getInstance();
        antiga.add(Calendar.DAY_OF_YEAR, -10);
        Participante participante = new Participante("Pedro Azarado");
        Jogo jogo1 = new CriadorDeJogo().para("Caça ao Tesouro").naData(antiga).resultado(participante, 300).constroi();

        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo1));
        doThrow(new RuntimeException("Erro simulado de banco de dados")).when(daoMock).atualiza(any(Jogo.class));

        try {
            finalizador.finalizaEEnviaSms();
        } catch (Exception e) {
            // Exceção esperada
        }

        verifyNoInteractions(smsMock);
    }
}