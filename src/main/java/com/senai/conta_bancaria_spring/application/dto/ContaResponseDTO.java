package com.senai.conta_bancaria_spring.application.dto;


import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.ContaCorrente;
import com.senai.conta_bancaria_spring.domain.entity.ContaPoupanca;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContaResponseDTO {
    private Long numero;
    private Double saldo;
    private String tipoConta;

    // Campos específicos que podem ser nulos
    private Long limite;
    private Double taxa;
    private Double rendimento;

    // Método de fábrica para converter a entidade em DTO
    public static ContaResponseDTO fromEntity(Conta conta) {
        ContaResponseDTO dto = new ContaResponseDTO();
        dto.setNumero(conta.getNumero());
        dto.setSaldo(conta.getSaldo());

        if (conta instanceof ContaCorrente cc) {
            dto.setTipoConta("CORRENTE");
            dto.setLimite(cc.getLimite());
            dto.setTaxa(cc.getTaxa());
        } else if (conta instanceof ContaPoupanca cp) {
            dto.setTipoConta("POUPANCA");
            dto.setRendimento(cp.getRendimento());
        }
        return dto;
    }
}
