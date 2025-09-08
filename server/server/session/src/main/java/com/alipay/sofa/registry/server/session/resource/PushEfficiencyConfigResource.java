package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.server.session.push.ChangeDebouncingTime;
import com.alipay.sofa.registry.server.session.push.ChangeProcessor;
import com.alipay.sofa.registry.server.shared.resource.AuthChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author huicha
 * @date 2025/9/8
 */
@Path("api/push/efficiency")
public class PushEfficiencyConfigResource {

  private Logger LOGGER = LoggerFactory.getLogger(PushEfficiencyConfigResource.class);

  @Autowired
  private ChangeProcessor changeProcessor;

  @GET
  @Path("/getChangeDebouncingMillis")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Map<String, ChangeDebouncingTime[]>> getChangeDebouncingMillis(@HeaderParam("token") String token) {
    try {
      if (!AuthChecker.authCheck(token)) {
        LOGGER.error("[module=PushEfficiencyConfigResource][method=getChangeDebouncingMillis] auth check={} fail!", token);
        return new GenericResponse().fillFailed("auth check fail");
      }

      Map<String, ChangeDebouncingTime[]> changeDebouncingTimes = this.changeProcessor.getChangeDebouncingMillis();
      return new GenericResponse().fillSucceed(changeDebouncingTimes);
    } catch (Throwable throwable) {
      LOGGER.error("[module=PushEfficiencyConfigResource][method=getChangeDebouncingMillis] getChangeDebouncingMillis exception", throwable);
      return new GenericResponse().fillFailed("getChangeDebouncingMillis exception");
    }
  }

}
