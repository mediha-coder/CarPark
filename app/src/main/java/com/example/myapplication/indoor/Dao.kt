package com.example.myapplication.indoor

/*import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrajectoryDao {

    @Insert
    suspend fun insertTrajectory(trajectory: Trajectory): Long

    @Insert
    suspend fun insertPoints(points: List<TrajectoryPoint>)

    @Query("SELECT * FROM trajectory_points WHERE trajectoryId = :trajectoryId")
    suspend fun getPointsForTrajectory(trajectoryId: Long): List<TrajectoryPoint>

    @Query("SELECT * FROM trajectories")
    suspend fun getAllTrajectories(): List<Trajectory>
}
*/