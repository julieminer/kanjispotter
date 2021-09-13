package com.melonheadstudios.kanjispotter.services

import android.content.Context
import android.content.Intent
import androidx.annotation.Nullable
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatViewInflater
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.managers.PrefManager
import com.melonheadstudios.kanjispotter.repos.KanjiRepo
import com.melonheadstudios.kanjispotter.views.HoverMenuScreen
import io.mattcarroll.hover.Content
import io.mattcarroll.hover.HoverMenu
import io.mattcarroll.hover.HoverView
import io.mattcarroll.hover.window.HoverMenuService
import java.util.*
import javax.inject.Inject


/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:51 PM
 */
class HoverPanelService: HoverMenuService() {
    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var kanjiRepo: KanjiRepo

    override fun onCreate() {
        MainApplication.graph.inject(this)
        updateTheme()
        super.onCreate()
    }

    override fun onDestroy() {
        kanjiRepo.clearAll()
        super.onDestroy()
    }

    override fun onHoverMenuLaunched(intent: Intent, hoverView: HoverView) {
        hoverView.setMenu(createHoverMenu())
        hoverView.collapse()
    }

    private fun createHoverMenu(): HoverMenu {
        return SingleSectionHoverMenu(applicationContext)
    }

    private fun updateTheme() {
        if (prefManager.darkThemeEnabled()) {
            setTheme(R.style.AppTheme)
        } else {
            setTheme(R.style.AppThemeLight)
        }
    }

    private class SingleSectionHoverMenu(private val context: Context) : HoverMenu() {
        private val section: HoverMenu.Section

        init {
            section = HoverMenu.Section(
                    HoverMenu.SectionId("1"),
                    createTabView(),
                    createScreen()
            )
        }

        private fun createTabView(): View {
            return View.inflate(context, R.layout.tab_view, null)
        }

        private fun createScreen(): Content {
            return HoverMenuScreen(context)
        }

        override fun getId(): String {
            return "hoverpanelservice"
        }

        override fun getSectionCount(): Int {
            return 1
        }

        @Nullable
        override fun getSection(index: Int): HoverMenu.Section? {
            return if (0 == index) {
                section
            } else {
                null
            }
        }

        @Nullable
        override fun getSection(sectionId: HoverMenu.SectionId): HoverMenu.Section? {
            return if (sectionId == section.id) {
                section
            } else {
                null
            }
        }

        override fun getSections(): List<HoverMenu.Section> {
            return Collections.singletonList(section)
        }
    }
}