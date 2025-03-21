package Clinica.Service;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Clinica.Agendamento.DadosListagemAgendamento;
import Clinica.Agendamento.RealizarAgendamentoDTO;
import Clinica.Endereco.EnderecoDTO;
import Clinica.Entities.Agendamento;
import Clinica.Entities.EnderecoUsuario;
import Clinica.Entities.Local;
import Clinica.Entities.Role;
import Clinica.Entities.Usuario;
import Clinica.Repository.AgendamentoRepository;
import Clinica.Repository.EndereçoRepository;
import Clinica.Repository.LocalRepository;
import Clinica.Repository.RoleRepository;
import Clinica.Repository.UsuarioRepository;
import Clinica.Usuarios.AlterarDadosDTO;
import Clinica.Usuarios.DadosCadastro;

@Service
public class UsuarioService {
	
	@Autowired 	
	RoleRepository roleRepository;
	
	@Autowired
	AgendamentoRepository agendamentoRepository;
	
	@Autowired
	LocalRepository localRepository;
	
	@Autowired 
	private PasswordEncoder password;
	
	@Autowired
	UsuarioRepository usuarioRepository;
	
	
	@Autowired
	EndereçoRepository endereçoRepository;
	
	public void cadastrarUsuario(DadosCadastro dados) {
		String codificado = password.encode(dados.senha());
        Usuario usuario = new Usuario(dados);
        Role role = roleRepository.findByRole("user");
        usuario.setRole(role);
        usuario.setSenha(codificado);
        usuarioRepository.save(usuario);
	}
	

	public EnderecoDTO cadastrarEndereço(EnderecoDTO enderecoUser) {
		EnderecoUsuario endereco = new EnderecoUsuario(enderecoUser);
		endereco.setUsuario(getUsuario());
		endereçoRepository.save(endereco);
		return new EnderecoDTO(endereco);
	}
	
	
	public RealizarAgendamentoDTO agendamentoCliente(RealizarAgendamentoDTO agendamentoCliente) {
		Usuario usuario = getUsuario();
		Agendamento agendamento = new Agendamento(agendamentoCliente);
		Local local = localRepository.findByEnderecoLocal(agendamentoCliente.enderecoLocal());
		agendamento.setLocal(local);
		agendamento.setUsuario(usuario);
		agendamentoRepository.save(agendamento);
		return new RealizarAgendamentoDTO(agendamento, local);
	}
	
	
	
	public List<DadosListagemAgendamento> listarAgendamentos(){
		Usuario usuario = getUsuario();
		List<Agendamento> agendamentos = usuario.getAgendamentos();
		List<Agendamento> agendamentoFiltro = agendamentos.stream().filter(ag -> Boolean.TRUE.equals(ag.getStatus())).collect(Collectors.toList());;
		List<DadosListagemAgendamento> agendamentoCliente = agendamentoFiltro.stream().map(agendar -> new DadosListagemAgendamento(agendar.getDataAgendamento(), agendar.getHoraAgendamento(), agendar.getSituacaoAgendamento(), agendar.getMotivoAgendamento()) ).collect(Collectors.toList());
		return agendamentoCliente;
	}
	
	
	
	public void cancelarAgendamento(Long id) {
	 Agendamento agendamento = agendamentoRepository.getReferenceById(id);
	 Boolean status = agendamento.getStatus();
	 if(status == true) {
		 agendamento.setStatus(false);
	 }
	}
	

	
	public Usuario getUsuario() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername(); 
            Usuario usuario = (Usuario) usuarioRepository.findByEmail(email);
            
            if(usuario == null) {
            	throw new RuntimeException("Nao foi possivel pegar o id");
            
            }
            return usuario;
        }
        throw new RuntimeException("erro inesperado");
    }
	
	public Long getUsuarioID() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String login = userDetails.getUsername(); 
            Usuario usuario = (Usuario) usuarioRepository.findByEmail(login);
            
            if(usuario == null) {
            	throw new RuntimeException("Nao foi possivel pegar o id");
            }
            return usuario.getId();
        }
        throw new RuntimeException("erro inesperado");
    }
	
	
	public AlterarDadosDTO alterDadosUsuario(AlterarDadosDTO alterar) {
		Usuario usuario = getUsuario();
		if(alterar.login() != null) {
			usuario.setLogin(alterar.login());
		}
		if(alterar.telefone() != null) {
			usuario.setTelefone(alterar.telefone());
		}
		
		return new AlterarDadosDTO(usuario);
	}

}
