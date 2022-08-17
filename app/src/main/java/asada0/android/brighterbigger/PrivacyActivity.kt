//
//  PrivacyActivity.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2022/07/28.
//  Copyright 2010-2022 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_manual.*
import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        privacy_view.loadUrl("file:///android_asset/" + getString(R.string.pref_privacy_html))
    }
}
