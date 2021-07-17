package com.ibug;

public interface IAnnotations {

    public @interface AbstractMethod {
        // todo
    }

    public @interface BackgroundTask {
        // todo
    }

    public @interface StartMethod {
        // todo
    }

    public @interface InnerClass {
        String value() default "[unassigned]";
    }

    public @interface Usage {
        String value() default "[unassigned]";
    }

    public @interface Constructor {
        // todo
    }

    public @interface Interface {
        // todo
    }

    public @interface RunOnce {
        // todo
    }

    public @interface UiTask {
        // todo
    }

    public @interface SystemRoot {
        // todo
    }

}
