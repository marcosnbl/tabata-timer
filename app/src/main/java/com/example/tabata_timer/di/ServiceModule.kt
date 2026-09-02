package com.example.tabata_timer.di

import com.example.tabata_timer.data.audio.AndroidSoundPlayer
import com.example.tabata_timer.domain.audio.SoundPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindSoundPlayer(
        androidSoundPlayer: AndroidSoundPlayer
    ): SoundPlayer
}
