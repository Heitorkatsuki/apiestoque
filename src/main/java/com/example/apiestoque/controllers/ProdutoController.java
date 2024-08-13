package com.example.apiestoque.controllers;

import com.example.apiestoque.models.Produto;
import com.example.apiestoque.repository.ProdutoRepository;
import com.example.apiestoque.services.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Rest: manipula nas praticas rest JSON
//Controller: Interação com o cliente

//RequestMapping: URL base
@RestController
@RequestMapping("api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;
    private final Validator validator;

//    Autowired: injeção de dependencia, criando o BIN(objeto de gerenciamento do container), pegando da classe
    @Autowired
    public ProdutoController(ProdutoService produtoService, Validator validator){
        this.produtoService = produtoService;
        this.validator = validator;
    }

    @GetMapping("/selecionar")
    @Operation(summary = "Listar todos os produtos",
    description = "Retorna uma lista de todos os produtos disponíveis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista os produtos disponiveis",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Produto.class))),
            @ApiResponse(responseCode = "500",description = "Erro interno do servidor", content = @Content)
    })
    public List<Produto> listarProdutos(){
        return produtoService.buscarTodosOsProdutos();
    }


//    RequestBody: trasforma o JSON em objeto
//    .ok: retorna um status ok com uma string
    @PostMapping("/inserir")
    public ResponseEntity<String> inserirProduto(@Valid @RequestBody Produto produto, BindingResult bindingResult){
        try{
            if(bindingResult.hasErrors()) return generateResponseError  (bindingResult);
            Produto prod = produtoService.salvarProduto(produto);
            if(isNotIdFine(prod.getId())) throw new RuntimeException();
            return ResponseEntity.ok("Produto inserido com sucesso");
        }catch(ClassCastException cce){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Os campos preco e quantidadeEstoque devem ser numéricos.");
        }catch (RuntimeException re){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro na requisição.");
        }
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Esclui produto por 10", description = "Remove um produto do sistema pelo seu 10")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Produto excluido com sucesso",
            content = @Content(mediaType = "applicarion/json",schema = @Schema(implementation = Produto.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ResponseEntity<String> excluirProduto(@PathVariable Long id){
           if(produtoService.buscarProdutoPorId(id).getId() == id){
               produtoService.excluirProduto(id);
               return ResponseEntity.ok("Produto excluido com sucesso!");
           }
           else {
            return ResponseEntity.status(404).body("Id não encontrado");
        }
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<String> atualizarProduto(@PathVariable Long id, @Valid @RequestBody Produto produtoAtualizado){
        Produto produtoExistente = produtoService.buscarProdutoPorId(id);
        if(produtoExistente.getId() == id){
            produtoExistente.setNome(produtoAtualizado.getNome());
            produtoExistente.setDescricao(produtoAtualizado.getDescricao());
            produtoExistente.setPreco(produtoAtualizado.getPreco());
            produtoExistente.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque());
            produtoService.salvarProduto(produtoExistente);
            return ResponseEntity.ok("Produto atualizado com sucesso");
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/atualizarParcial/{id}")
    public ResponseEntity<?> atualizarProdutoParcial(@PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Mapeamento de campos a serem atualizados com os novos valores",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"nome\": \"Novo nome\",}"+
                            ""))
            )@RequestBody Map<String, Object> updates){
        try{
            Produto produto = produtoService.buscarProdutoPorId(id);
            if(updates.containsKey("nome")){
                produto.setNome((String) updates.get("nome"));
            }
            if(updates.containsKey("descricao")){
                produto.setDescricao((String) updates.get("descricao"));
            }

            if(updates.containsKey("preco")){
                try{
                    produto.setPreco((Double) updates.get("preco"));
                }catch (ClassCastException cce){
                    int precoint = (Integer) updates.get("preco");
                    produto.setPreco(Double.parseDouble(String.valueOf(precoint)));
                }
            }

            if(updates.containsKey("quantidadeEstoque")){
                produto.setQuantidadeEstoque((Integer) updates.get("quantidadeEstoque"));
            }
//            Vincula o validator com a entidade
            DataBinder binder = new DataBinder(produto);
            binder.setValidator(validator);
            binder.validate();
            BindingResult resultado = binder.getBindingResult();
            if(resultado.hasErrors()){
                Map erros = validarProduto(resultado);
                return ResponseEntity.badRequest().body(erros);
            }
            Produto produtoSalvo = produtoService.salvarProduto(produto);
            return ResponseEntity.ok(produtoSalvo);
        }catch (RuntimeException re){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto nao encontrado");
        }

    }
    public static boolean isNotIdFine(Long id){
        return id <= 0;
    }
    public static ResponseEntity<String> generateResponseError(BindingResult result){
        String message = "";
        result.getFieldErrors();
        for (FieldError error : result.getFieldErrors()) {
            message += error.getDefaultMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    public Map<String, String> validarProduto(BindingResult resultado){
        Map<String, String> erros = new HashMap<>();
        for (FieldError error : resultado.getFieldErrors()){
            erros.put(error.getField(), error.getDefaultMessage());
        }
        return erros;
    }

    @GetMapping("/buscarPorNome")
    public ResponseEntity<?> buscarPorNome(@RequestParam String nome, @RequestParam double preco){
        List<Produto> listaProduto = produtoService.buscarPorNome(nome, preco);
        if(!listaProduto.isEmpty()){
            return ResponseEntity.ok(listaProduto);
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado");
        }
    }
}