package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object;

import android.content.Intent;
import android.net.Uri;

import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.CharacterGenerator;

/**
 * Created by ken on 02/12/17 for fantastic_beasts
 */

public class UriGenerator implements IValueGenerator<Uri> {

    private final static String[] knownUriSchemes = {"content", "file", "tel", "http", "ftp",
            "sip", "mail", "contact"};

    @Override
    public Uri random() {
        Uri.Builder b = new Uri.Builder();
        b.scheme(knownUriSchemes[ValueGeneratorManager.r.nextInt(knownUriSchemes.length)]);

        int size = ValueGeneratorManager.r.nextInt(ConfigurationManager.getUriMaxNodes());
        for (int i = 0; i < size; i++){
            b.appendPath(new StringGenerator().random());
        }

        return b.build();
    }

    @Override
    public Uri mutate(Uri mutant) throws ValueGeneratorException {
        try {

            if (mutant == null)
                return random();

            Uri.Builder b = mutant.buildUpon();

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    if (ValueGeneratorManager.r.nextBoolean())
                        return null;
                    else
                        b.scheme(knownUriSchemes[ValueGeneratorManager.r.nextInt(knownUriSchemes.length)]);
                case NEIGHBOR:
                case INVERSE:
                    int p = ValueGeneratorManager.r.nextInt(5);

                    switch (p){
                        case 0:
                            b.authority(new StringGenerator().mutate(mutant.getAuthority()));
                            break;
                        case 1:
                            b.fragment(new StringGenerator().mutate(mutant.getFragment()));
                            break;
                        case 2:
                            b.path(new StringGenerator().mutate(mutant.getPath()));
                            break;
                        case 3:
                            b.query(new StringGenerator().mutate(mutant.getQuery()));
                            break;
                        case 4:
                            b.scheme(new StringGenerator().mutate(mutant.getScheme()));
                            break;
                        default:
                            break;
                    }
            }

            return b.build();
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate " + mutant, e);
        }

    }

    @Override
    public Uri crossover(Uri parent1, Uri parent2) throws ValueGeneratorException {
        return null;
    }
}
