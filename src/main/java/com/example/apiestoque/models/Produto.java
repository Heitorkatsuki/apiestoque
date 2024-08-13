package com.example.apiestoque.models;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Schema(description = "Representa um produto no sistema")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Schema(description = "ID único do produto", example = "1234")
    private long id;
    @NotNull
    @Size(min = 2, message = "O nome deve conter no mínimo 2 caracteres")
    @Schema(description = "Nome do produto", example = "Hamburguer de frango")
    private String nome;
    @Schema(description = "Descrição detalhada do produto",
            example = "Hamburguer de frango congelado de 500g")
    private String descricao;
    @NotNull
    @Min(value = 0,message = "O valor deve ser maior que zero")
    @Schema(description = "Preço do produto",example = "1999.99")
    private double preco;
    @NotNull
    @Min(value = 0,message = "O estoque deve ser maior que 0")
    @Column(name = "quantidadeestoque")
    @Schema(description = "Quantidade do estoque",example = "80")
    private int quantidadeEstoque;

    public Produto(long id, String nome, String descricao, double preco, int quantidadeEstoque) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }
    public Produto(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", preco=" + preco +
                ", quantidadeEstoque=" + quantidadeEstoque +
                '}';
    }
}
