package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.CreateMcpToken;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CreateTokenResponse;
import id.my.tudemaha.lobos.dto.response.McpTokenData;
import id.my.tudemaha.lobos.dto.response.McpTokenList;
import id.my.tudemaha.lobos.dto.response.PaginationResponse;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;
import id.my.tudemaha.lobos.exception.InvalidTokenException;
import id.my.tudemaha.lobos.mapper.McpTokenMapper;
import id.my.tudemaha.lobos.model.McpToken;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.repository.McpTokenRepository;
import id.my.tudemaha.lobos.repository.UserRepository;
import id.my.tudemaha.lobos.utils.Pagination;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class McpTokenService {
    private final McpTokenRepository mcpTokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    public McpTokenService(
            McpTokenRepository mcpTokenRepository,
            UserRepository userRepository
    ) {
        this.mcpTokenRepository = mcpTokenRepository;
        this.userRepository = userRepository;
    }

    private McpToken isTokenEmpty(String id) {
        Optional<McpToken> mcpTokenOpt = mcpTokenRepository.getMcpTokenById(id);
        if (mcpTokenOpt.isEmpty()) {
            throw new InvalidTokenException();
        }

        return mcpTokenOpt.get();
    }

    public CreateTokenResponse generateToken(CreateMcpToken createMcpToken, String userId) {
        McpToken mcpToken = McpTokenMapper.toEntity(createMcpToken);
        mcpToken.setUserId(userId);

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        mcpToken.setToken(Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes)
        );

        mcpTokenRepository.insert(mcpToken);

        CreateTokenResponse createTokenResponse = new CreateTokenResponse();
        createTokenResponse.setToken(mcpToken.getToken());

        return createTokenResponse;
    }

    public User authenticate(String token) {
        Optional<McpToken> mcpTokenOpt = mcpTokenRepository.getMcpTokenByToken(token);
        if (mcpTokenOpt.isEmpty()) {
            throw new InvalidTokenException();
        }

        McpToken mcpToken = mcpTokenOpt.get();
        Optional<User> userOpt = userRepository.findById(mcpToken.getUserId());
        if (userOpt.isEmpty()) {
            throw new InvalidTokenException();
        }

        mcpTokenRepository.updateLastUsed(mcpToken);

        return userOpt.get();
    }

    public McpTokenList getMcpTokensByUserId(String userId, PaginationRequest paginationRequest) {
        PaginationRequest validPaginationRequest = Pagination.buildPaginationRequest(paginationRequest);
        McpTokenRepository.PaginatedMcpToken paginatedMcpToken = mcpTokenRepository.findAllByUserId(userId, validPaginationRequest);

        List<McpTokenData> mcpTokenData = paginatedMcpToken.mcpTokens()
                .stream()
                .map(McpTokenMapper::toDto)
                .toList();

        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setPage(validPaginationRequest.getPage());
        paginationResponse.setTotalPage(
                Math.ceilDiv(paginatedMcpToken.totalCount(), validPaginationRequest.getPerPage())
        );

        McpTokenList mcpTokenList = new McpTokenList();
        mcpTokenList.setMcpTokens(mcpTokenData);
        mcpTokenList.setPagination(paginationResponse);

        return mcpTokenList;
    }

    public void updateName(CreateMcpToken createMcpToken, String tokenId, String userId) {
        McpToken mcpToken = isTokenEmpty(tokenId);
        if (!mcpToken.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("User is not allowed to delete token");
        }

        mcpToken.setName(createMcpToken.getName());
        mcpTokenRepository.updateName(mcpToken);
    }

    public void delete(String tokenId, String userId) {
        McpToken mcpToken = isTokenEmpty(tokenId);
        if(!mcpToken.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("User is not allowed to delete token");
        }

        mcpTokenRepository.delete(mcpToken.getId());
    }
}
