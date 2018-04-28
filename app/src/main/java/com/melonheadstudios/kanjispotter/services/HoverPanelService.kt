package com.melonheadstudios.kanjispotter.services

import android.content.Context
import android.content.Intent
import android.support.annotation.Nullable
import android.view.View
import android.widget.ImageView
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.views.HoverMenuScreen
import io.mattcarroll.hover.Content
import io.mattcarroll.hover.HoverMenu
import io.mattcarroll.hover.HoverView
import io.mattcarroll.hover.window.HoverMenuService
import java.util.*


/**
 * kanjispotter
 * Created by jake on 2018-04-28, 3:51 PM
 */
class HoverPanelService: HoverMenuService() {
    override fun onHoverMenuLaunched(intent: Intent, hoverView: HoverView) {
        hoverView.setMenu(createHoverMenu())
        hoverView.collapse()
    }

    private fun createHoverMenu(): HoverMenu {
        return SingleSectionHoverMenu(applicationContext)
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
            val imageView = ImageView(context)
            imageView.setImageResource(R.drawable.tab_background)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            return imageView
        }

        private fun createScreen(): Content {
            return HoverMenuScreen(context, "Screen 1")
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