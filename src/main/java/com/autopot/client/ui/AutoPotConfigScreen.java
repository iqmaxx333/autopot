package com.autopot.client.ui;

import com.autopot.AutoPotConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class AutoPotConfigScreen extends Screen {

    private final Screen parent;
    private final AutoPotConfig workingCopy;
    private final Consumer<AutoPotConfig> saveConsumer;

    private ButtonWidget activationKeyButton;
    private boolean awaitingKey = false;

    public AutoPotConfigScreen(Screen parent, AutoPotConfig config, Consumer<AutoPotConfig> saveConsumer) {
        super(Text.translatable("screen.autopot.title"));
        this.parent = parent;
        this.workingCopy = config.copy();
        this.saveConsumer = saveConsumer;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int rowHeight = 28;

        // Activation key button
        activationKeyButton = ButtonWidget.builder(
                getActivationLabel(),
                btn -> {
                    awaitingKey = true;
                    activationKeyButton.setMessage(Text.translatable("screen.autopot.awaiting"));
                })
                .dimensions(centerX - 100, startY, 200, 20)
                .build();
        this.addDrawableChild(activationKeyButton);

        // Toggle buttons
        addToggle(centerX, startY + rowHeight,     "Fire Resistance",
                workingCopy::isUseFireResistance, workingCopy::setUseFireResistance);
        addToggle(centerX, startY + rowHeight * 2, "Strength",
                workingCopy::isUseStrength,       workingCopy::setUseStrength);
        addToggle(centerX, startY + rowHeight * 3, "Speed",
                workingCopy::isUseSpeed,           workingCopy::setUseSpeed);
        addToggle(centerX, startY + rowHeight * 4, "Auto Look Down",
                workingCopy::isAutoLookDown,       workingCopy::setAutoLookDown);

        // Save & Close
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.autopot.save"),
                btn -> closeWithSave())
                .dimensions(centerX - 75, this.height - 30, 150, 20)
                .build());
    }

    private void addToggle(int centerX, int y, String label,
                           java.util.function.BooleanSupplier getter,
                           Consumer<Boolean> setter) {
        this.addDrawableChild(ButtonWidget.builder(
                toggleLabel(label, getter.getAsBoolean()),
                btn -> {
                    boolean next = !getter.getAsBoolean();
                    setter.accept(next);
                    btn.setMessage(toggleLabel(label, next));
                })
                .dimensions(centerX - 100, y, 200, 20)
                .build());
    }

    private Text toggleLabel(String label, boolean enabled) {
        return Text.literal(label + ": " + (enabled ? "ON" : "OFF"));
    }

    private Text getActivationLabel() {
        return Text.literal("Activation Key: " + workingCopy.getActivationKey().getLocalizedText().getString());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (awaitingKey) {
            InputUtil.Key key = InputUtil.fromKeyCode(keyCode, scanCode);
            workingCopy.setActivationKey(key);
            activationKeyButton.setMessage(getActivationLabel());
            awaitingKey = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void closeWithSave() {
        saveConsumer.accept(workingCopy);
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void close() {
        // Close without saving (pressing Escape)
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
