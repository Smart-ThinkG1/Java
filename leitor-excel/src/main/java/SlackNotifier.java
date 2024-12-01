import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersListResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlackNotifier {
    private final String token;
    private final Slack slack;

    // Construtor que lê o token a partir da variável de ambiente - teste
    public SlackNotifier()
    {
        this.token = System.getenv("SLACK_API_TOKEN");
        if (this.token == null)
        {
            throw new IllegalArgumentException("Token do Slack não está definido na variável de ambiente SLACK_API_TOKEN.");
        }
        this.slack = Slack.getInstance();
    }

    public Map<String, String> listarUsuariosSlack()
    {
        Map<String, String> usuarios = new HashMap<>();
        try {
            UsersListResponse response = slack.methods(token).usersList(r -> r);
            if (response.isOk())
            {
                response.getMembers().forEach(user ->
                {
                    String userId = user.getId();
                    String email = user.getProfile() != null && user.getProfile().getEmail() != null ?
                            user.getProfile().getEmail() : null;

                    System.out.println(user.getName());
                    System.out.println(user.getProfile().getEmail());
                    System.out.println(user.getId());
                    System.out.println(user.getProfile());

                    if (email != null)
                    {
                        usuarios.put(email, userId);
                    }
                });
            }
            else
            {
                System.out.println("Erro ao listar usuários: " + response.getError());
            }
        }
        catch (IOException | SlackApiException e)
        {
            e.printStackTrace();
        }
        return usuarios;
    }

    public void enviarNotificacao(String userId, String mensagem)
    {
        try
        {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(userId)
                    .text(mensagem)
                    .build();

            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(request);
            if (response.isOk()) {
                System.out.println("Mensagem enviada ao usuário com sucesso!");
            } else {
                System.out.println("Erro ao enviar mensagem: " + response.getError());
            }
        } catch (IOException | SlackApiException e)
        {
            e.printStackTrace();
        }
    }
}