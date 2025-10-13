package api.util;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.Getter;

import java.io.IOException;

@Getter
public class ContentLengthTrackingResponseWrapper extends HttpServletResponseWrapper {
  private int contentLength = 0;

  public ContentLengthTrackingResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public void setContentLength(int len) {
    this.contentLength = len;
    super.setContentLength(len);
  }

  @Override
  public void setContentLengthLong(long len) {
    this.contentLength = (int) len;
    super.setContentLengthLong(len);
  }

}

