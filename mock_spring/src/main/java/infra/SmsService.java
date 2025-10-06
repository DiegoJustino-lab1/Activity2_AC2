package infra;

public class SmsService {

    public void enviar(String nomeDoVencedor) {
        System.out.println("SMS enviado para: " + nomeDoVencedor);
    }

    // Método main para testar o envio de SMS
    public static void main(String[] args) {
        SmsService sms = new SmsService();
        sms.enviar("João Vencedor");
    }
}