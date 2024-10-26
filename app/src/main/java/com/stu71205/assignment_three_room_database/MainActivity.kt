package com.stu71205.assignment_three_room_database


import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val jobDao = MainApplication.jobDatabase.jobDao()
                JobOrganizerScreen(jobDao)
            }
        }
    }
}

@Composable
fun JobOrganizerScreen(jobDao: JobDao) {
    val factory = JobViewModelFactory(jobDao)
    val viewModel: JobViewModel = viewModel(factory = factory)
    val jobList by viewModel.jobList.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Job Organizer",
            style = typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }
        var customerName by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var jobType by remember { mutableStateOf("") }

        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start Date") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End Date") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = jobType,
            onValueChange = { jobType = it },
            label = { Text("Job Type") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(onClick = {
                val job = JobOrganizer(
                    startDate = startDate,
                    endDate = endDate,
                    customerName = customerName,
                    location = location,
                    jobType = jobType
                )
                viewModel.addJob(job)
            }) {
                Text("Add Job")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                if (jobList.isNotEmpty()) {
                    viewModel.deleteJob(jobList.last().id)
                }
            }) {
                Text("Delete Job")
            }
        }

        LazyColumn {
            items(jobList) { job ->
                JobItem(job)
            }
        }
    }
}

@Composable
fun JobItem(job: JobOrganizer) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text("Start Date: ${job.startDate}")
        Text("End Date: ${job.endDate}")
        Text("Customer Name: ${job.customerName}")
        Text("Location: ${job.location}")
        Text("Job Type: ${job.jobType}")
    }
}

//#################################################################################################
@Dao
interface JobDao {

    @Query("SELECT * FROM JobOrganizer")
    fun getAllJobs(): LiveData<List<JobOrganizer>>

    @Insert
    fun insertJob(job: JobOrganizer)

    @Query("DELETE FROM JobOrganizer where id = :id")
    fun deleteAllJobs(id: Int)
}

@Entity
data class JobOrganizer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startDate: String,
    val endDate: String,
    val customerName: String,
    val location: String,
    val jobType: String
)

class JobViewModel(jobDao1: JobDao) : ViewModel() {
    val jobDao: JobDao = MainApplication.jobDatabase.jobDao()
    val jobList: LiveData<List<JobOrganizer>> = jobDao.getAllJobs()

    fun addJob(job: JobOrganizer) {
        viewModelScope.launch(Dispatchers.IO) {
            jobDao.insertJob(job)
        }
    }

    fun deleteJob(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            jobDao.deleteAllJobs(id)
        }
    }
}

class JobViewModelFactory(private val jobDao: JobDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobViewModel(jobDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainApplication : Application() {
    companion object {
        lateinit var jobDatabase: JobDatabase
    }

    override fun onCreate() {
        super.onCreate()
        jobDatabase = Room.databaseBuilder(
            applicationContext,
            JobDatabase::class.java,
            "job_database"
        ).build()
    }
}

@Database(entities = [JobOrganizer::class], version = 1, exportSchema = false)
abstract class JobDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
}
