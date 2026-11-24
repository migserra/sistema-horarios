package br.com.unisales.horarios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_SEGURANCA = "basicAuth";

    @Bean
    public OpenAPI configuracaoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gestão de Horários de Professores")
                        .version("1.0.0")
                        .description("""
                                API REST para organização e gerenciamento de horários de aula de professores.

                                Permite o CRUD de professores, disciplinas, turmas e horários, além da
                                consulta da grade por professor e por turma. A API impede conflitos de
                                horário para o mesmo professor, turma e disciplina.

                                **Autenticação:** HTTP Basic. Utilize o usuário padrão `admin` e a senha
                                `admin123` (configuráveis por variáveis de ambiente). No Swagger UI,
                                clique em **Authorize** e informe usuário e senha.
                                """))
                .components(new Components().addSecuritySchemes(ESQUEMA_SEGURANCA,
                        new SecurityScheme()
                                .name(ESQUEMA_SEGURANCA)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Autenticação HTTP Basic com usuário e senha")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURANCA));
    }
}
