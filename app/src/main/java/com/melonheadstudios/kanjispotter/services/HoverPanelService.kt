package com.melonheadstudios.kanjispotter.services

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.annotation.Nullable
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.views.HoverMenuScreen
import io.mattcarroll.hover.Content
import io.mattcarroll.hover.HoverMenu
import io.mattcarroll.hover.HoverView
import io.mattcarroll.hover.window.HoverMenuService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.*


/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:51 PM
 */
class HoverPanelService: HoverMenuService() {
    override fun onCreate() {
        updateTheme()
        super.onCreate()
    }

    override fun onDestroy() {
        MainApplication.instance.kanjiRepo.clearAll()
        super.onDestroy()
    }

    override fun onHoverMenuLaunched(intent: Intent, hoverView: HoverView) {
        hoverView.setMenu(createHoverMenu())
        hoverView.collapse()
    }

    private fun createHoverMenu(): HoverMenu {
        return SingleSectionHoverMenu(applicationContext)
    }

    private fun updateTheme() = runBlocking {
        if (MainApplication.instance.dataStore.darkThemeEnabled.firstOrNull() != false) {
            setTheme(R.style.AppTheme)
        } else {
            setTheme(R.style.AppThemeLight)
        }
    }

    private class SingleSectionHoverMenu(private val context: Context) : HoverMenu() {
        private val section: Section

        init {
            section = Section(
                    SectionId("1"),
                    createTabView(),
                    createScreen()
            )
        }

        private fun createTabView(): View {
            return View.inflate(context, R.layout.tab_view, null)
        }

        private fun createScreen(): Content {
            return HoverMenuScreen(MainApplication.instance.kanjiRepo, MainApplication.instance.scope, context)
        }

        override fun getId(): String {
            return "hoverpanelservice"
        }

        override fun getSectionCount(): Int {
            return 1
        }

        @Nullable
        override fun getSection(index: Int): Section? {
            return if (0 == index) {
                section
            } else {
                null
            }
        }

        @Nullable
        override fun getSection(sectionId: SectionId): Section? {
            return if (sectionId == section.id) {
                section
            } else {
                null
            }
        }

        override fun getSections(): List<Section> {
            return Collections.singletonList(section)
        }
    }
}