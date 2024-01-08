package ipca.game.hash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hash)


        val buttonHome = findViewById<Button>(R.id.idButtonHome)


        buttonHome.setOnClickListener{
            finish()
        }
    }
}