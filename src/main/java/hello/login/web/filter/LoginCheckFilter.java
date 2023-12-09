package hello.login.web.filter;

import hello.login.web.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LoginCheckFilter implements Filter {
    private static final String[] whitelist = {"/", "/members/add", "/login", "/logout", "/css/*"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            log.info("認証チェックフィルタースタート{}", requestURI);
            if (isLoginCheckPath(requestURI)) {
                log.info("認証チェックロジック実行 {}", requestURI);
                HttpSession session = httpRequest.getSession(false);
                if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
                    log.info("未認証使用者リクエスト {}", requestURI);
                    // ログインでredirect
                    httpResponse.sendRedirect("/login?redirectURL=" + requestURI);
                    return; // ここが重要、未認証使用者は次に実行しなくて終わり
                }
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            throw e; //例外ロギングも可能ですが、tomcatまで例外を出す必要がある
        } finally {
            log.info("認証チェックフィルター終了{}", requestURI);
        }
    }

    /**
     * ファイトリストの場合、認証チェックX
     */
    private boolean isLoginCheckPath(String requestURI) {
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }
}