package br.com.keoma.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.keoma.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var authorization = request.getHeader("Authorization");
    if (authorization == null) {
      response.sendError(401, "Usuário sem autorização");
      return;
    }

    var encodedAuth = authorization.substring("Basic".length()).trim();

    byte[] decodedAuth = Base64.getDecoder().decode(encodedAuth);

    var authString = new String(decodedAuth);

    String[] credentials = authString.split(":");
    String username = credentials[0];
    String password = credentials[1];

    var user = this.userRepository.findByUsername(username);
    if (user == null) {
      response.sendError(401, "Usuário sem autorização");
      return;
    }

    var passwordVerifier = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
    if (!passwordVerifier.verified) {
      response.sendError(401, "Usuário sem autorização");
      return;
    }

    request.setAttribute("userId", user.getId());
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    var path = request.getServletPath();
    return !path.startsWith("/tasks");
  }
}
