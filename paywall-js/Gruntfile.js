module.exports = function(grunt) {
    'use strict';

    // Project configuration.
    grunt.initConfig({
        jasmine : {
            src : 'src/**/*.js',
            options : {
                specs : 'spec/**/*Spec.js',
                helpers: 'spec/helpers/*.js',
                vendor: [
                      "node_modules/stompjs/lib/stomp.js",
                      "node_modules/sockjs-client/dist/sockjs.js"
                    ]
            }
        },
        jshint: {
            all: [
                'Gruntfile.js',
                'src/**/*.js',
                'spec/**/*.js'
            ],
            options: {
               // jshintrc: '.jshintrc'
            }
        },
        jsdoc : {
            dist : {
                src: ['src/*.js', 'README.md'],
                options: {
                    destination: 'build/doc'
                }
            }
        },
        strip_code: {
            options: {
                start_block: "/* test-code */",
                end_block: "/* end-test-code */"
            },
            your_target: {
                // a list of files you want to strip code from
                files: [
                    {src: 'src/paywall.src.js', dest: 'build/dist/paywall.js'},
                ]
            }
        },
        copy: {
            main: {
                files: [
                    // includes files within path
                    {expand: true, flatten: true, src: ['node_modules/stompjs/lib/stomp.js'], dest: 'build/dist/', filter: 'isFile'},
                    {expand: true, flatten: true, src: ['node_modules/stompjs/lib/stomp.min.js'], dest: 'build/dist/', filter: 'isFile'},
                    {expand: true, flatten: true, src: ['node_modules/sockjs-client/dist/*.js'], dest: 'build/dist/', filter: 'isFile'},

                ]
            }
        },
        uglify : {

            options : {
                banner : "/*! paywall.min.js file */\n"
            },
            build : {
                src : ["build/dist/paywall.js"],
                dest : "build/dist/paywall.min.js"
            }

        }
    });

    grunt.loadNpmTasks('grunt-contrib-jasmine');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-jsdoc');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-strip-code');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.registerTask('test', ['jshint', 'jasmine']);

    // TODO figure out how gradle and grunt works together, jasmine should be possible from gradle and added to test
    grunt.registerTask('default', ['test']);


    grunt.registerTask("deploy", [
        "strip_code",
        //"jshint",
        "uglify",
        "copy"
    ]);

};