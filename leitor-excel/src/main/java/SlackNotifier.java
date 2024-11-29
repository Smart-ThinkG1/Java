import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersListResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlackNotifier
{
    private static final String TOKEN = "xoxb-8113017461600-8115125090816-HamiIpU1bLGItvqUeXxYv8qf";
    private final Slack slack;

    public SlackNotifier()
    {
        this.slack = Slack.getInstance();
    }

    public Map<String, String> listarUsuariosSlack()
    {
        Map<String, String> usuarios = new HashMap<>();
        try
        {
            UsersListResponse response = slack.methods(TOKEN).usersList(r -> r);
            if (response.isOk())
            {
                response.getMembers().forEach(user ->
                {
                    String userId = user.getId();
                    // Verifica se o perfil não é nulo e se contém o email
                    String email = user.getProfile() != null && user.getProfile().getEmail() != null ?
                            user.getProfile().getEmail() : null;

                    System.out.println(user.getName());
                    System.out.println(user.getProfile().getEmail());
                    System.out.println(user.getId());
                    System.out.println(user.getProfile());

                    if (email != null)
                    { // Verifica se email não é nulo e não está vazio
                        usuarios.put(email, userId); // Mapeia e-mail para userId
                    }
                });
            }
            else
            {
                System.out.println("Erro ao listar usuários: " + response.getError());
            }
        } catch (IOException | SlackApiException e)
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

            ChatPostMessageResponse response = slack.methods(TOKEN).chatPostMessage(request);
            if (response.isOk())
            {
                System.out.println("Mensagem enviada ao usuário com sucesso!");
            }
            else
            {
                System.out.println("Erro ao enviar mensagem: " + response.getError());
            }
        }
        catch (IOException | SlackApiException e)
        {
            e.printStackTrace();
        }
    }
}