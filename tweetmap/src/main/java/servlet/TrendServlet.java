package servlet;

import com.google.gson.Gson;
import com.likethecolor.alchemy.api.Client;
import com.likethecolor.alchemy.api.call.AbstractCall;
import com.likethecolor.alchemy.api.call.RankedKeywordsCall;
import com.likethecolor.alchemy.api.call.type.CallTypeText;
import com.likethecolor.alchemy.api.entity.KeywordAlchemyEntity;
import com.likethecolor.alchemy.api.entity.Response;
import db.Rds;
import db.Tweet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by slgu1 on 10/18/15.
 */
public class TrendServlet extends HttpServlet {
    static Rds rds;
    static LinkedList <String> trends = new LinkedList<String>();
    //init
    static {
        rds = new Rds();
        try {
            final String apiKey = "";
            final Client client = new Client();
            rds.init();
            LinkedList<Tweet> res = rds.getSample(100);
            int n = res.size();
            StringBuilder builder = new StringBuilder();
            builder.append(res.get(0).getText());
            for (int i = 1; i < n; ++i) {
                builder.append(" ");
                builder.append(res.get(i).getText());
            }
            String textPara = builder.toString();
            client.setAPIKey(apiKey);
            final AbstractCall<KeywordAlchemyEntity> authorCall = new RankedKeywordsCall(new CallTypeText(textPara));
            Response<KeywordAlchemyEntity> resp = client.call(authorCall);
            Iterator<KeywordAlchemyEntity> iter = resp.iterator();
            int total = 10;
            while (iter.hasNext() && ((total--) != 0)) {
                KeywordAlchemyEntity keyword = iter.next();
                trends.add(keyword.getKeyword());
            }
        }
        catch (Exception e) {

        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write(new Gson().toJson(trends));
    }
}
