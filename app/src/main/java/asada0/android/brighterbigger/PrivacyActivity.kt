//
//  PrivacyActivity.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2023/10/08.
//  Copyright 2010-2023 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

//import kotlinx.android.synthetic.main.activity_manual.*
//import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        optimizeEdgeToEdge()

        val privacyView = findViewById<WebView>(R.id.privacy_view)
        privacyView.loadUrl("file:///android_asset/" + getString(R.string.pref_privacy_html))
    }

    private fun optimizeEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.privacy_view)) { root, windowInsets ->
            /*
            var actionBarHeight: Int = 0
            val tv = TypedValue()
            if (this.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }
             */
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.updatePadding(
                top = 0,
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                //topMargin = actionBarHeight
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }
}
