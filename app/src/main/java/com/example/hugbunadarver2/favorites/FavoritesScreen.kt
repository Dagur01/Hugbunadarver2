import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hugbunadarver2.home.Movie
import com.example.hugbunadarver2.home.MovieCard


@Composable
fun FavoritesScreen(
    movies: List<Movie>,
    favoriteIds: Set<Long>,
    onToggleFavorite: (Long) -> Unit
) {
    val favMovies = movies.filter { favoriteIds.contains(it.movieId) }

    if (favMovies.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No favorites yet")
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = favMovies,
            key = { it.movieId }
        ) { movie ->
            MovieCard(
                movie = movie,
                isFavorite = true,
                onToggleFavorite = { onToggleFavorite(movie.movieId) }
            )
        }
    }
}