package org.jboss.as.console.client.shared.model;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.jboss.as.console.client.core.BootstrapContext;
import org.jboss.as.console.client.domain.model.ProfileRecord;
import org.jboss.as.console.client.shared.BeanFactory;
import org.jboss.as.console.client.shared.dispatch.DispatchAsync;
import org.jboss.as.console.client.shared.dispatch.impl.DMRAction;
import org.jboss.as.console.client.shared.dispatch.impl.DMRResponse;
import org.jboss.dmr.client.ModelDescriptionConstants;
import org.jboss.dmr.client.ModelNode;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.dmr.client.ModelDescriptionConstants.*;

/**
 * @author Heiko Braun
 * @date 3/18/11
 */
public class SubsystemStoreImpl implements SubsystemStore {

    private DispatchAsync dispatcher;
    private BeanFactory factory = GWT.create(BeanFactory.class);
    private BootstrapContext bootstrap;

    @Inject
    public SubsystemStoreImpl(DispatchAsync dispatcher, BootstrapContext bootstrap) {
        this.dispatcher = dispatcher;
        this.bootstrap = bootstrap;
    }

    @Override
    public void loadSubsystems(String profileName, final AsyncCallback<List<SubsystemRecord>> callback) {

        ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_CHILDREN_NAMES_OPERATION);
        operation.get(CHILD_TYPE).set("subsystem");
        operation.get(ADDRESS).setEmptyList();

        if(bootstrap.getProperty(BootstrapContext.STANDALONE).equals("false"))
        {
            operation.get(ADDRESS).add("profile", "default");  //TODO: doesn't work with multiple profiles
        }

        dispatcher.execute(new DMRAction(operation), new AsyncCallback<DMRResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(DMRResponse result) {
                ModelNode response = ModelNode.fromBase64(result.getResponseText());
                List<ModelNode> payload = response.get("result").asList();

                List<SubsystemRecord> records = new ArrayList<SubsystemRecord>(payload.size());
                for(int i=0; i<payload.size(); i++)
                {
                    SubsystemRecord record = factory.subsystem().as();
                    record.setTitle(payload.get(i).asString());
                    records.add(record);
                }

                callback.onSuccess(records);
            }
        });
    }
}
