package barqsoft.footballscores.api;

import java.util.List;

import barqsoft.footballscores.models.Fixture;
import barqsoft.footballscores.models.Team;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by gabrielquiles-perez on 11/18/15.
 */
public interface FootballDataService {
    @GET("/teams/{id}")
    Call<Team> getTeam(@Path("id") Long team_id);

    @GET("/fixtures/")
    Call<List<Fixture>> getFixtures(@Query("timeFrame") String timeframe, @Query("league") String league);
}
