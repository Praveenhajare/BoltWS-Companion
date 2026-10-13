const fs = require('fs');
const filePath = 'src/com/supertech/BoltWS/BoltWS.java';

// ?? ??? ??? ???? ????? ??? ??? ???? ???? ?? ??? ???? ???? ???
let code = fs.readFileSync(filePath, 'utf8');

// ????? ??? ??? ?? ??? ???? $add ??? ??? ???? ?? ?? ???
const cleanDynamicAdd = `                        if ("$add".equals(mName)) {
                            if (args != null && args.length > 0 && args[0] instanceof AndroidViewComponent) {
                                final View cv = ((AndroidViewComponent) args[0]).getView();
                                if (cv != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            View root = defaultTargetComponent != null ? defaultTargetComponent.getView() : null;
                                            if (root instanceof ViewGroup) {
                                                if (cv.getParent() instanceof ViewGroup) {
                                                    ((ViewGroup) cv.getParent()).removeView(cv);
                                                }
                                                // ??? ??? ???: ??? ?? ?? dp ??
                                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT, 
                                                    dipToPx(170)
                                                );
                                                lp.setMargins(dipToPx(16), dipToPx(18), dipToPx(16), dipToPx(24));
                                                cv.setLayoutParams(lp);
                                                cv.setMinimumHeight(dipToPx(170));

                                                if (cv instanceof ViewGroup) {
                                                    ViewGroup vg = (ViewGroup) cv;
                                                    for (int c = 0; c < vg.getChildCount(); c++) {
                                                        View inner = vg.getChildAt(c);
                                                        ViewGroup.LayoutParams inLp = inner.getLayoutParams();
                                                        if (inLp != null) {
                                                            inLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                                            inLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                                            inner.setLayoutParams(inLp);
                                                        }
                                                    }
                                                }

                                                ((ViewGroup) root).addView(cv);
                                                extensionInjectedViews.add(cv);
                                                cv.requestLayout();
                                                cv.invalidate();
                                            }
                                        }
                                    });
                                }
                            }
                            return null;
                        }`;

// ?? ??? ??? ?? ??? ?? ???? ???
const fsBuild = require('./build_clean_boltws.js');
