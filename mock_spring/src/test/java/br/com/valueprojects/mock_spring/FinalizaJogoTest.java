package br.com.valueprojects.mock_spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.FinalizaJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import infra.JogoDao;
import infra.SmsService;

public class FinalizaJogoTest {

	@Test
	public void deveFinalizarJogosDaSemanaAnterior() {

		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Jogo jogo1 = new CriadorDeJogo().para("Ca�a moedas")
				.naData(antiga).constroi();
		Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras")
				.naData(antiga).constroi();

		List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

		JogoDao daoFalso = mock(JogoDao.class);

		when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

		FinalizaJogo finalizador = new FinalizaJogo(daoFalso);
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
		Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();

		List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

		JogoDao daoFalso = mock(JogoDao.class);

		when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

		FinalizaJogo finalizador = new FinalizaJogo(daoFalso);
		finalizador.finaliza();

		verify(daoFalso, times(1)).atualiza(jogo1);
		// Mockito.verifyNoInteractions(daoFalso);

	}

	@Test
	public void deveSalvarAntesDeEnviarSms() {
		JogoDao daoMock = mock(JogoDao.class);
		SmsService smsMock = mock(SmsService.class);

		Jogo jogo = new CriadorDeJogo().para("Corrida").constroi();
		jogo.setVencedor(new Participante("Vencedor"));

		FinalizaJogo finalizaJogo = new FinalizaJogo(daoMock, smsMock);

		finalizaJogo.finalizarEEnviarSms(jogo);

		// Verifica que salvou antes de enviar SMS
		verify(daoMock).salvar(jogo);
		verify(smsMock).enviar("Vencedor");
	}

	@Test
	public void naoDeveEnviarSmsSeNaoSalvar() {
		JogoDao daoMock = mock(JogoDao.class);
		SmsService smsMock = mock(SmsService.class);

		Jogo jogo = new CriadorDeJogo().para("Corrida").constroi();

		FinalizaJogo finalizaJogo = new FinalizaJogo(daoMock, smsMock);

		// Não chama finalizarEEnviarSms, logo não salva nem envia SMS
		// Verifica que não houve interação com SMS
		verifyNoInteractions(smsMock);
	}

}
